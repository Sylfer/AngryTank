package angrygeneration;

import robocode.*;

import java.awt.*;
import java.awt.Event;
import java.awt.geom.Point2D;

import static java.lang.Math.signum;
import static java.lang.Math.toRadians;

import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

public class AngryTankJr extends AdvancedRobot {
	
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
			getMovement().move(enemyX, enemyY);
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
		
		final double alphaToEnemy = getHeadingRadians() + first_e.getBearingRadians();
		
		enemyX = getX() + Math.sin(alphaToEnemy) * first_e.getDistance();
	    enemyY = getY() + Math.cos(alphaToEnemy) * first_e.getDistance();			
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
	}