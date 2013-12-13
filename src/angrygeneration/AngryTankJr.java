package angrygeneration;

import robocode.*;
import robocode.util.Utils;

import java.awt.*;
import java.awt.Event;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import static java.lang.Math.signum;
import static java.lang.Math.toRadians;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;


public class AngryTankJr extends AdvancedRobot {
	
    public static int BINS = 47;
    public static double _surfStats[] = new double[BINS];	
	
    public Point2D.Double _myLocation;     // our bot's location
    public Point2D.Double _enemyLocation;  // enemy bot's location
	
    public ArrayList _enemyWaves;
    public ArrayList _surfDirections;
    public ArrayList _surfAbsBearings;
    
    public static Rectangle2D.Double _fieldRect
    = new java.awt.geom.Rectangle2D.Double(18, 18, 764, 564);
    
    public static double WALL_STICK = 160;    
    
    public static double _oppEnergy = 100.0;    
    
	/** Точность вычислений */
	public static final double PRECISION = 1e-06;
	/** 2*Pi */
	public static final double DOUBLE_PI = Math.PI * 2;
	/** Pi/2 */
	public static final double HALF_PI = Math.PI/ 2;
	/** Максимальная скорость поворота пушки */
	public static final double MAX_GUN_ROTATION_SPEED = Math.toRadians(20);
	/** Максимальная линейная скорость */
	public static final double MAX_SPEED = 8;
	/** Максимальная энергия выстрела */
	public static final double MAX_FIRE_POWER = 3;
	/** Базовая энергия выстрела */
	public static final double BASE_FIRE_POWER = MAX_FIRE_POWER / 2.0;
	/** Сколько времени можно не стрелять */
	public static final double FIRE_DELAY_CRITICAL = 400;
	/** Нормальный уровень энергии */
	public static final double ENERGY_NORMAL_THRESHOLD = 50;
	/** Опасный уровень энергии */
	public static final double ENERGY_WARNING_THRESHOLD = 30;
	/** Критически опасный уровень энергии */
	public static final double ENERGY_CRITICAL_THRESHOLD = 15;
	
	/////////////////////
	/** Размеры поля */
	public double _battleFieldWidth, _battleFieldHeight;
	/** Размеры робота */
	public double _robotSize;
	/** Половинный размер робота */
	public double _robotSizeHalved;
	/** Допуск при определении коллизии*/
	public double _collisionDelta;
	/** Текущее количество других роботов*/
	public int _aliveRobotsCount;
	/** Номер текущего шага */
	public long _currentTime;
	/** Генератор случайных чисел */
	public Random _randomizer = new Random();
	/** Текущий уровень энергии */
	public double _currentEnegy;
	/** Момент последнего выстрела */
	public long _lastShotTime;
	/** Количество попаданий */
	public long _hits;
	/** Количество промахов */
	public long _misses;
	/** Количество попаданий в нас */
	public long _hittesByBullet;
	/** Количество столкновений со стенами */
	public long _wallCollisions;
	/** Радар робота */
	private Radar _radar;
	/** Водитель робота */
	private Driver _driver;
	/** Стрелок робота */
	private Gunner _gunner;
	/** Список целей */
	private TargetList _targets;
	/** Список событий */
	public Vector<robocode.Event> _events;
	/** Текущий объект-состояние */
	private SuperVisorState _currentState;
	/** Новый водитель*/
	public Movement _movement;
    
	private double enemyX = -1;
    private double enemyY = -1;	
	
	
	/** Получить текущее состояние */
	public SuperVisorState getCurrentState() {
		return _currentState;
	}
	/** Изменить текущее состояние */
	public void setCurrentState(SuperVisorState aCurrentState) {
		_currentState = aCurrentState;
	}	
	
	/* Метод, вызываемый в начале каждого шага*/
	@Override
	public void run() {
		
        _enemyWaves = new ArrayList();
        _surfDirections = new ArrayList();
        _surfAbsBearings = new ArrayList();		
		
		// Вызвать автомат A0 с событием "Начало раунда"
		SuperVisorState.processIncomingEvent(Constants.EVENT_ROUND_START, this);

		while (true) {
			_currentTime = getTime();
			Logger.log("--------- " + _currentTime + " ---------");
			// Обработать очередь событий
			check_events();
			
			// Вызвать автомат A0 с событием "Начало шага"
			SuperVisorState.processIncomingEvent(Constants.EVENT_STEP_START, this);
			getGunner().beginTurn(); // Передать событие "Начало шага" объекту "Стрелок"
			getRadar().beginTurn(); // Передать событие "Начало шага" объекту "Радар"
			//getMovement().move(enemyX, enemyY);
			//getDriver().beginTurn(); // Передать событие "Начало шага" объекту "Водитель"
			endTurnEvent();
			
			// Обработать событие "Конец шага"
			execute();
		}
	}
	
	
	
	/** Обработка события среды Robocode “Попадание в цель” */
	public void onBulletHit(BulletHitEvent first_e) {
		_events.add(first_e);
	}
	/** Обработка события среды Robocode “Попадание в стену” */
	public void onBulletMissed(BulletMissedEvent first_e) {
		_events.add(first_e);
	}
	/** Обработка события среды Robocode “Попадание в другую пулю” */
	public void onBulletHitBullet(BulletHitBulletEvent first_e) {
		_events.add(first_e);
	}
	/** Обработка события среды Robocode “Обновление цели ” */
	public void onScannedRobot(ScannedRobotEvent first_e) {
		_events.add(first_e);
		
		/*final double alphaToEnemy = getHeadingRadians() + first_e.getBearingRadians();
		
		enemyX = getX() + Math.sin(alphaToEnemy) * first_e.getDistance();
	    enemyY = getY() + Math.cos(alphaToEnemy) * first_e.getDistance();*/

        _myLocation = new Point2D.Double(getX(), getY());
        
        double lateralVelocity = getVelocity()*Math.sin(first_e.getBearingRadians());
        double absBearing = first_e.getBearingRadians() + getHeadingRadians();
 
        setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing
            - getRadarHeadingRadians()) * 2);
 
        _surfDirections.add(0,
            new Integer((lateralVelocity >= 0) ? 1 : -1));
        _surfAbsBearings.add(0, new Double(absBearing + Math.PI));
 
 
        double bulletPower = _oppEnergy - first_e.getEnergy();
        if (bulletPower < 3.01 && bulletPower > 0.09
            && _surfDirections.size() > 2) {
            EnemyWave ew = new EnemyWave();
            ew.fireTime = getTime() - 1;
            ew.bulletVelocity = bulletVelocity(bulletPower);
            ew.distanceTraveled = bulletVelocity(bulletPower);
            ew.direction = ((Integer)_surfDirections.get(2)).intValue();
            ew.directAngle = ((Double)_surfAbsBearings.get(2)).doubleValue();
            ew.fireLocation = (Point2D.Double)_enemyLocation.clone(); // last tick
 
            _enemyWaves.add(ew);
        }
 
        _oppEnergy = first_e.getEnergy();
 
        // update after EnemyWave detection, because that needs the previous
        // enemy location as the source of the wave
        _enemyLocation = project(_myLocation, absBearing, first_e.getDistance());
 
        updateWaves();
        doSurfing();		
		
	}
	/** Обработка события среды Robocode “Уничтожение цели” */
	public void onRobotDeath(RobotDeathEvent first_e) {
		_events.add(first_e);
	}
	/** Обработка события среды Robocode “Столкновение с целью” */
	public void onHitRobot(HitRobotEvent first_e) {
		_events.add(first_e);
	}
	/** Обработка события среды Robocode “Столновение со стеной ” */
	public void onHitWall(HitWallEvent first_e) {
		_events.add(first_e);
	}
	/** Обработка события среды Robocode “Попадание победа”*/
	public void onHitByBullet(HitByBulletEvent first_e) {
		_events.add(first_e);
		
        // If the _enemyWaves collection is empty, we must have missed the
        // detection of this wave somehow.
        if (!_enemyWaves.isEmpty()) {
            Point2D.Double hitBulletLocation = new Point2D.Double(
            		first_e.getBullet().getX(), first_e.getBullet().getY());
            EnemyWave hitWave = null;
 
            // look through the EnemyWaves, and find one that could've hit us.
            for (int x = 0; x < _enemyWaves.size(); x++) {
                EnemyWave ew = (EnemyWave)_enemyWaves.get(x);
 
                if (Math.abs(ew.distanceTraveled -
                    _myLocation.distance(ew.fireLocation)) < 50
                    && Math.abs(bulletVelocity(first_e.getBullet().getPower()) 
                        - ew.bulletVelocity) < 0.001) {
                    hitWave = ew;
                    break;
                }
            }
 
            if (hitWave != null) {
                logHit(hitWave, hitBulletLocation);
 
                // We can remove this wave now, of course.
                _enemyWaves.remove(_enemyWaves.lastIndexOf(hitWave));
            }
        }		
	}
	/** Обработка события среды Robocode “Победа”*/
	public void onWin(WinEvent first_e) {
		SuperVisorState.processIncomingEvent(Constants.EVENT_WIN, this);
	}
	
	/** Обработка события среды Robocode “Поражение” */
	public void onDeath(DeathEvent first_e) {
		SuperVisorState.processIncomingEvent(Constants.EVENT_DEATH, this);
	}
	/** Конструктор, устанавливающий объект-logger и переводящий автомат в начальное состояние */
	public AngryTankJr() {
		Logger._out = out;
		SuperVisorState.reset(this);
	}
	/** Создание частей танка – “Радар”, ”Водитель”, ”Стрелок”,” Cписок целей”*/
	private void createDevices() {
		setTargets(new TargetList(this));
		setRadar(new Radar(this));
		setMovement(new Movement(this));
		setDriver(new Driver(this));
		setGunner(new Gunner(this));
	}
	/** Установка основных констант и параметров */
	private void setUpParameters() {
		_battleFieldWidth = getBattleFieldWidth();
		_battleFieldHeight = getBattleFieldHeight();
		_robotSize = (getWidth() + getHeight()) / 2.0;
		_robotSizeHalved = _robotSize / 2.0;
		_collisionDelta = _robotSizeHalved - 5;
		_hits = 0;
		_misses = 0;
		_hittesByBullet = 0;
		_wallCollisions = 0;
	}
	/** Установка приоритета событий */
	private void setUpPriorities() {
		setEventPriority("RobotDeathEvent", 17);
		setEventPriority("ScannedRobotEvent", 16);
		setEventPriority("HitRobotEvent", 15);
		setEventPriority("HitWallEvent", 14);
		setEventPriority("BulletHitEvent", 13);
		setEventPriority("HitByBulletEvent", 12);
		setEventPriority("BulletMissedEvent", 11);
	}
	/*
	Реализация выходных воздействий
	*/
	/** z10_0 : Инициализация при запуске */
	protected void z10_0_initializeAtStart() {
		if (Constants.OUTPUTS_LOGGING)
			Logger.logOutput("z10_0", "Инициализация при запуске");
		
		setUpPriorities();
		setUpParameters();
		createDevices();
		_events = new Vector<robocode.Event>();
	}
	/** z10_1 : Инициализация в начале раунда*/
	protected void z10_1_initializeAtNewRound() {
		if (Constants.OUTPUTS_LOGGING)
			Logger.logOutput("z10_1", "Инициализация в начале раунда");
		Logger.log("***");
		Logger.log("*** Раунд " + (getRoundNum() + 1));
		Logger.log("***");
		clearAllEvents();
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		_currentTime = 0;
		getTargets().beginRound();
		getDriver().beginRound();
		getRadar().beginRound();
		getGunner().beginRound();
		if (_events != null) _events.clear();
	}
	/** z10_2 : Инициализация в начале шага */
	protected void z10_2_requestInputParametersAtNewStep() {
		if (Constants.OUTPUTS_LOGGING) {
			Logger.logOutput("z10_2", "Инициализация в начале шага");
		}
		
		_aliveRobotsCount = getOthers();
		_currentEnegy = getEnergy();
		getTargets().beginTurn();
	}
	/** z20 : Вывод статистики по раунду */
	protected void z20_printRoundStatistics() {
		if (Constants.OUTPUTS_LOGGING) {
			Logger.logOutput("z20", "Вывести статистику раунда");
		}
		showStatistics();
	}
	/*
	Вспомогательные методы
	*/
	/** Считать все события из очереди */
	private void check_events() {
		
		Iterator<robocode.Event> eventIterator = _events.iterator();
		robocode.Event event;
		
		while (eventIterator.hasNext()) {
			
			event = eventIterator.next();
			if (event.getClass().equals(ScannedRobotEvent.class)) {
				getTargets().update(ScannedRobotEvent.class.cast(event));
			} else if (event.getClass().equals(BulletHitEvent.class)) {
				getTargets().hit(BulletHitEvent.class.cast(event));
				_hits++;
			} else if (event.getClass().equals(RobotDeathEvent.class)) {
				getTargets().targetDestroyed(RobotDeathEvent.class.cast(event));
			} else if (event.getClass().equals(HitRobotEvent.class)) {
				DriverState.processIncomingEvent(Constants.EVENT_ENEMY_COLLISION, getDriver());
				getTargets().collision(HitRobotEvent.class.cast(event));
			} else if (event.getClass().equals(HitWallEvent.class)) {
				DriverState.processIncomingEvent(Constants.EVENT_WALL_COLLISION, getDriver());
				_wallCollisions++;
			} else if (event.getClass().equals(BulletMissedEvent.class)) {
				_misses++;
			} else if (event.getClass().equals(HitByBulletEvent.class)) {
				DriverState.processIncomingEvent(Constants.EVENT_HIT_BY_BULLET, getDriver());
				getTargets().hitByBullet(HitByBulletEvent.class.cast(event));
				_hittesByBullet++;
			}
		}
		
		_events.clear();
	}
		/** Конец шага */
		private void endTurnEvent() {
			//getDriver().endTurn(); // Передать событие "Начало шага" объекту "Водитель"
			getRadar().endTurn(); // Передать событие "Начало шага" объекту "Радар"
			getGunner().endTurn(); // Передать событие "Начало шага" объекту "Стрелок"
		}
		/** Вывод статистики по раунду */
		private void showStatistics() {
			long shots = _hits + _misses;
			getTargets().showStatistics();
			Logger.log("Выстрелов: " + shots + ", попаданий: " + _hits + ", промахов: " + _misses);
			Logger.log("Меткость: " + (double) _hits / shots);
			Logger.log("Попали в нас: " + _hittesByBullet);
			Logger.log("Столкновений со стенами: " + _wallCollisions);
		}
		/** Расчитать скорость выстрела заданной мощности */
		public static double getBulletSpeed(double firepower) {
			return (20 - 3 * firepower);
		}
		/** Приведение угла в диапазон от 0 до 2PI */
		public static double normalizeAngle(double a) {
			a = DOUBLE_PI + (a % DOUBLE_PI);
			a %= DOUBLE_PI;
			return a;
		}
		/** Определить минимальную разницу между
		двумя углами с учетом перехода через ноль */
		public static double getAngleDiff(double from, double to) {
			double diff = to - from;
			if (Math.abs(diff) <= Math.PI) return diff;
			if (diff < 0)
				diff += DOUBLE_PI;
			else if (diff > 0)
				diff -= DOUBLE_PI;
			return diff % DOUBLE_PI;
		}
		/**Вычислить угловую координату вектора */
		public static double getAngle(double x, double y) {
			double a;
			if (y == 0) {
				return x > 0 ? HALF_PI : 3 * HALF_PI;
			}
			a = Math.atan(x / y);
			if (y < 0) a += Math.PI;
				return a;
		}
		/** Получить радар данного робота */
		public Radar getRadar() {
			return _radar;
		}
		/** Установить радар для данного робота */
		private void setRadar(Radar aRadar) {
			_radar = aRadar;
		}
		/** Получить водителя для данного робота */
		public Driver getDriver() {
			return _driver;
		}
		/** Установить водителя для данного робота */
		private void setDriver(Driver aDriver) {
			_driver = aDriver;
		}
		/** Получить движение для данного робота */
		public Movement getMovement() {
			return _movement;
		}
		/** Установить движение для данного робота */
		private void setMovement(Movement aMovement) {
			_movement = aMovement;
		}	
		/** Получить стрелка для данного робота */
		public Gunner getGunner() {
			return _gunner;
		}
		/** Установить стрелка для данного робота */
		private void setGunner(Gunner aGunner) {
			_gunner = aGunner;
		}
		/** Получить список целей для данного робота */
		public TargetList getTargets() {
			return _targets;
		}
		/** Установить список целей для данного робота */
		private void setTargets(TargetList aTargets) {
			_targets = aTargets;
		}
		
		class EnemyWave {
	        Point2D.Double fireLocation;
	        long fireTime;
	        double bulletVelocity, directAngle, distanceTraveled;
	        int direction;
	 
	        public EnemyWave() { }
	    }
		
	    public static double bulletVelocity(double power) {
	        return (20.0 - (3.0*power));
	    }
	    
	    public void doSurfing() {
	        EnemyWave surfWave = getClosestSurfableWave();
	 
	        if (surfWave == null) { return; }
	 
	        double dangerLeft = checkDanger(surfWave, -1);
	        double dangerRight = checkDanger(surfWave, 1);
	 
	        double goAngle = absoluteBearing(surfWave.fireLocation, _myLocation);
	        if (dangerLeft < dangerRight) {
	            goAngle = wallSmoothing(_myLocation, goAngle - (Math.PI/2), -1);
	        } else {
	            goAngle = wallSmoothing(_myLocation, goAngle + (Math.PI/2), 1);
	        }
	 
	        setBackAsFront(this, goAngle);
	    }
	    
	    public void updateWaves() {
	        for (int x = 0; x < _enemyWaves.size(); x++) {
	            EnemyWave ew = (EnemyWave)_enemyWaves.get(x);
	 
	            ew.distanceTraveled = (getTime() - ew.fireTime) * ew.bulletVelocity;
	            if (ew.distanceTraveled >
	                _myLocation.distance(ew.fireLocation) + 50) {
	                _enemyWaves.remove(x);
	                x--;
	            }
	        }
	    } 
	    
	    public static Point2D.Double project(Point2D.Double sourceLocation,
	            double angle, double length) {
	            return new Point2D.Double(sourceLocation.x + Math.sin(angle) * length,
	                sourceLocation.y + Math.cos(angle) * length);
	        }
	    
	    public EnemyWave getClosestSurfableWave() {
	        double closestDistance = 50000; // I juse use some very big number here
	        EnemyWave surfWave = null;
	 
	        for (int x = 0; x < _enemyWaves.size(); x++) {
	            EnemyWave ew = (EnemyWave)_enemyWaves.get(x);
	            double distance = _myLocation.distance(ew.fireLocation)
	                - ew.distanceTraveled;
	 
	            if (distance > ew.bulletVelocity && distance < closestDistance) {
	                surfWave = ew;
	                closestDistance = distance;
	            }
	        }
	 
	        return surfWave;
	    }
	    
	    public double checkDanger(EnemyWave surfWave, int direction) {
	        int index = getFactorIndex(surfWave,
	            predictPosition(surfWave, direction));
	 
	        return _surfStats[index];
	    }
	    
	    public double wallSmoothing(Point2D.Double botLocation, double angle, int orientation) {
	        while (!_fieldRect.contains(project(botLocation, angle, WALL_STICK))) {
	            angle += orientation*0.05;
	        }
	        return angle;
	    }
	    
	    public static void setBackAsFront(AdvancedRobot robot, double goAngle) {
	        double angle =
	            Utils.normalRelativeAngle(goAngle - robot.getHeadingRadians());
	        if (Math.abs(angle) > (Math.PI/2)) {
	            if (angle < 0) {
	                robot.setTurnRightRadians(Math.PI + angle);
	            } else {
	                robot.setTurnLeftRadians(Math.PI - angle);
	            }
	            robot.setBack(100);
	        } else {
	            if (angle < 0) {
	                robot.setTurnLeftRadians(-1*angle);
	           } else {
	                robot.setTurnRightRadians(angle);
	           }
	            robot.setAhead(50);
	        }
	    }
	    
	    public static double absoluteBearing(Point2D.Double source, Point2D.Double target) {
	        return Math.atan2(target.x - source.x, target.y - source.y);
	    }
	    
	    public Point2D.Double predictPosition(EnemyWave surfWave, int direction) {
	        Point2D.Double predictedPosition = (Point2D.Double)_myLocation.clone();
	        double predictedVelocity = getVelocity();
	        double predictedHeading = getHeadingRadians();
	        double maxTurning, moveAngle, moveDir;
	 
	        int counter = 0; // number of ticks in the future
	        boolean intercepted = false;
	 
	        do {    // the rest of these code comments are rozu's
	            moveAngle =
	                wallSmoothing(predictedPosition, absoluteBearing(surfWave.fireLocation,
	                predictedPosition) + (direction * (Math.PI/2)), direction)
	                - predictedHeading;
	            moveDir = 1;
	 
	            if(Math.cos(moveAngle) < 0) {
	                moveAngle += Math.PI;
	                moveDir = -1;
	            }
	 
	            moveAngle = Utils.normalRelativeAngle(moveAngle);
	 
	            // maxTurning is built in like this, you can't turn more then this in one tick
	            maxTurning = Math.PI/720d*(40d - 3d*Math.abs(predictedVelocity));
	            predictedHeading = Utils.normalRelativeAngle(predictedHeading
	                + limit(-maxTurning, moveAngle, maxTurning));
	 
	            // this one is nice ;). if predictedVelocity and moveDir have
	            // different signs you want to breack down
	            // otherwise you want to accelerate (look at the factor "2")
	            predictedVelocity +=
	                (predictedVelocity * moveDir < 0 ? 2*moveDir : moveDir);
	            predictedVelocity = limit(-8, predictedVelocity, 8);
	 
	            // calculate the new predicted position
	            predictedPosition = project(predictedPosition, predictedHeading,
	                predictedVelocity);
	 
	            counter++;
	 
	            if (predictedPosition.distance(surfWave.fireLocation) <
	                surfWave.distanceTraveled + (counter * surfWave.bulletVelocity)
	                + surfWave.bulletVelocity) {
	                intercepted = true;
	            }
	        } while(!intercepted && counter < 500);
	 
	        return predictedPosition;
	    }

	    public static int getFactorIndex(EnemyWave ew, Point2D.Double targetLocation) {
	        double offsetAngle = (absoluteBearing(ew.fireLocation, targetLocation)
	            - ew.directAngle);
	        double factor = Utils.normalRelativeAngle(offsetAngle)
	            / maxEscapeAngle(ew.bulletVelocity) * ew.direction;
	 
	        return (int)limit(0,
	            (factor * ((BINS - 1) / 2)) + ((BINS - 1) / 2),
	            BINS - 1);
	    }
	    
	    public static double limit(double min, double value, double max) {
	        return Math.max(min, Math.min(value, max));
	    }
	    
	    public static double maxEscapeAngle(double velocity) {
	        return Math.asin(8.0/velocity);
	    }
	    
	    public void logHit(EnemyWave ew, Point2D.Double targetLocation) {
	        int index = getFactorIndex(ew, targetLocation);
	 
	        for (int x = 0; x < BINS; x++) {
	            // for the spot bin that we were hit on, add 1;
	            // for the bins next to it, add 1 / 2;
	            // the next one, add 1 / 5; and so on...
	            _surfStats[x] += 1.0 / (Math.pow(index - x, 2) + 1);
	        }
	    }	    
	}