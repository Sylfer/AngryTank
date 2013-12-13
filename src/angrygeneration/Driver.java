package angrygeneration;

import robocode.util.Utils;

public class Driver {

	/*
	Константы
	*/
	/** Коэффициент "отталкивания" от стен */
	private final double WALLS_COEFF = 100;
	/** Допустимое отклонение */
	private final double HEADING_DELTA = Math.toRadians(30);
	/*
	Переменные объекта
	*/
	/** Момент срабатывания таймера _t110 */
	private long _t110;
	/** Направление движения*/
	private double _oldHeading, _curHeading;
	/** Координаты*/
	private double _oldX, _oldY, _curX, _curY;
	/** Текущая скорость поворота */
	private double _turningSpeed;
	/** Линейная скорость */
	private double _curSpeed;
	/** На сколько надо повернуть на данном шаге*/
	private double _da;
	/** С какой скоростью надо двигаться на данном шаге */
	private double _speed;
	/** Направление движения */
	private GeomVektor _direction = new GeomVektor();
	/** Текущее состояние*/
	private DriverState _currentState;
	/** Объект-супервизор */
	AngryTankJr _robot;
	/**
	* Получить текущее состояние
	* @return Объект, представляющий текущее состояние
	*/
	public DriverState getCurrentState() {
		return _currentState;	
	}
	/**
	* Установить текущее состояние
	* @param aCurrentState новое состояние
	*/
	public void setCurrentState(DriverState aCurrentState) {
		_currentState = aCurrentState;
	}
	/**
	* Создает объект "Водитель" для данного робота-супервизора
	* @param aRobot супервизор
	*/
	public Driver(AngryTankJr aRobot) {
		_robot = aRobot;
		DriverState.reset(this);
	}
	/** Метод, вызываемый в начале каждого раунда*/
	public void beginRound() {
		_oldHeading = _robot.getHeadingRadians();
		_curHeading = _robot.getHeadingRadians();
		_curSpeed = 0;
		_oldX = _curX = _robot.getX();
		_oldY = _curY = _robot.getY();
		_turningSpeed = 0;
		_t110 = 0;
		_direction.reset();
		_da = 0;
		_speed = AngryTankJr.MAX_SPEED;
	}
	/** Метод, вызываемый в начале каждого шага */
	public void beginTurn() {
		_oldHeading = _curHeading;
		_curHeading = _robot.getHeadingRadians();
		_oldX = _curX;
		_curX = _robot.getX();
		_oldY = _curY;
		_curY = _robot.getY();
		_curSpeed = Math.sqrt(Math.pow(_curX - _oldX, 2) + Math.pow(_curY - _oldY, 2));
		_curSpeed = _curSpeed * (_speed >= 0 ? 1 : -1);
		_turningSpeed = _robot.getAngleDiff(_oldHeading, _curHeading);
		_da = 0;
		DriverState.processIncomingEvent(Constants.EVENT_STEP_START, this);
	}
	/** Метод, вызываемый в конце шага*/
	public void endTurn() {
	_robot.setTurnRightRadians(_da);
	// Движение вперед
	_robot.setAhead(_speed * 100);
	}
	/**
	* Вернуть скорость поворота
	* @return скорость поворота
	*/
	public double getTurningSpeed() {
		return _turningSpeed;
	}	
	/*
	Реализация входных переменных
	*/
	/** x100 : Проверка на наличие близкого врага */
	public boolean x100_enemyIsNear() {
		boolean result = true;
		if (_robot.getTargets()._closestTarget != null) {
			result = _robot.getTargets()._closestTarget._distance < 300;
		}
		return result;
	}
	/** x105 : Проверка на близость к стене */
	public boolean x105_wallIsNear() {
		double collision_delta = _robot._robotSizeHalved + 40;
		boolean result =
		_curX < 0 + collision_delta
		|| _curX > _robot._battleFieldWidth - collision_delta
		|| _curY < 0 + collision_delta
		|| _curY > _robot._battleFieldHeight - collision_delta;
		return result;
	}
	/** x110 : Проверка на срабатывание таймера T110 */
	public boolean x110_timeoutExpired() {
		boolean result = _robot._currentTime >= _t110;
		return result;
	}
	/*
	Реализация выходных воздействий
	*/
	/** z200_0 : Инициализация движения по траектории 'Маятник' */
	public void z200_0_initializePendulumTrajectory() {
		_robot.setMaxVelocity(10);
		getDirection();
	}
	/** z200_1 : Прибавление случайной составляющей к траектории 'Маятник' */
	public void z200_1_randomizePendulumTrajectory() {
		_direction.setCoords(_direction.getAngle(), 1);
		double angle_diff = _robot.normalizeAngle(_direction.getAngle() +
		(_robot._randomizer.nextBoolean() ? AngryTankJr.HALF_PI : -AngryTankJr.HALF_PI));
		_direction.addRadialVector(angle_diff, 0.4);
	}
	/** z200_2 : Пересчет параметров при движении по траектории 'Маятник' */
	public void z200_2_calculatePendulumTrajectory() {
		calculate_movement_order(true);
	}
	/** z210_0 : Инициализация движения по траектории 'Дуга' */
	public void z210_0_initializeArcTrajectory() {
		double TTT = _robot.getTargets()._closestTarget != null
		? _robot.getTargets()._closestTarget._distance / 20.0 : 10;
		TTT = TTT * (_robot._randomizer.nextDouble() * 0.5 + 0.8);
		_t110 = _robot._currentTime + Math.round(TTT);
		getDirection();
		_robot.setMaxVelocity(10);
		if (_robot.getTargets()._closestTarget != null) {
			double delta1 =
					Math.abs(_robot.getAngleDiff(_direction.getAngle(), _robot.getTargets()._closestTarget._angle));
			double delta2 =
					Math.abs(_robot.getAngleDiff(_direction.getAngle(),
							_robot.normalizeAngle(_robot.getTargets()._closestTarget._angle + Math.PI)));
			if (delta1 > HEADING_DELTA || delta2 > HEADING_DELTA) {
				_direction.setAngle(_robot.normalizeAngle(_robot.getTargets()._closestTarget._angle + Math.PI
				+ (_robot._randomizer.nextBoolean() ? AngryTankJr.HALF_PI / 2 : -AngryTankJr.HALF_PI / 2)));
			}
		}
	}
	/** z210_1 : Добавление случайной составляющей к траектории 'Дуга' */
	public void z210_1_randomizeArcTrajectory() {
	}
	/** z210_2 : Определиние направления и скорости движения 'Дуга' */
	public void z210_2_calculateArcTrajectory() {
		calculate_movement_order(false);
		_da = 0;
	}
	/** z220_0 : Инициализация движения по траектории 'Уклонение' */
	public void z220_0_initializeDigressionTrajectory() {
		_t110 = _robot._currentTime + 17;
		getDirection();
		_direction.setAngle(-AngryTankJr.HALF_PI * (_direction.getAngle() / Math.abs(_direction.getAngle())));
		_robot.setMaxVelocity(5);
	}
	/** z220_1 : Добавление случайной составляющей к траектории 'Уклонение' */
	public void z220_1_randomizeDigressionTrajectory() {
	}
	/** z220_2 : Определение направления движения 'Уклонение' */
	public void z220_2_calculateDigressionTrajectory() {
		calculate_movement_order(false);
	}
	/** z230_0 : Инициализация движения по траектории 'Останов' */
	public void z230_0_initializeStopTrajectory() {
		double TTT = _robot.getTargets()._closestTarget != null
				? _robot.getTargets()._closestTarget._distance / 20.0 : 10;
		TTT = TTT * (_robot._randomizer.nextDouble() * 0.3 + 0.3);
		_t110 = _robot._currentTime + Math.round(TTT);
		_robot.setMaxVelocity(0);
	}
	///////////////////////////////////////////////////////////////////////////
	/** z240_0 : Инициализация движения по траектории 'Син' */
	public void z240_0_initializeSinTrajectory() {
		
		_t110 = _robot._currentTime + 17;
		//getDirection();
		//_direction.setAngle(-AngryTankJr.HALF_PI * (_direction.getAngle() / Math.abs(_direction.getAngle())));
	    final double lateralDirection = Math.signum((_curSpeed != 0 ? _curSpeed : 1) * Math.sin(Utils.normalRelativeAngle(_curHeading - _da)));
		
	    //_robot.setMaxVelocity(5);
		
	}
	/** z240_1 : Добавление случайной составляющей к траектории 'Уклонение' */
	public void z240_1_randomizeDigressionTrajectory() {
	}
	/** z240_2 : Определение направления движения 'Уклонение' */
	public void z240_2_calculateDigressionTrajectory() {
		calculate_movement_order(false);
	}

	
	/*
	Вспомогательные методы
	*/
	/** Получить направление движения на удаление от целей и стен */
	private void getDirection() {
		_direction.reset();
		_direction.addCatresianVector(WALLS_COEFF / _curX, 0);
		_direction.addCatresianVector(-WALLS_COEFF / (_robot._battleFieldWidth - _curX), 0);
		_direction.addCatresianVector(0, WALLS_COEFF / _curY);
		_direction.addCatresianVector(0, -WALLS_COEFF / (_robot._battleFieldHeight - _curY));
		_direction.add(_robot.getTargets().getReactionVector());
	}
	/** Вычислить требуемый угол поворота */
	private void calculate_movement_order(boolean aReverseAllowed) {
		double da1, // Угол, на который надо повернуться при движении вперед
		da2; // Угол, на который надо повернуться при движении назад
		da1 = _robot.getAngleDiff(_curHeading, _direction.getAngle());
		da2 = _robot.getAngleDiff(_curHeading, _robot.normalizeAngle(_direction.getAngle() - Math.PI));
		if (aReverseAllowed) {
			if (Math.abs(da1) < Math.abs(da2)) {
				_da = da1;
				_speed = AngryTankJr.MAX_SPEED;
			} else {
				_da = da2;
				_speed = -AngryTankJr.MAX_SPEED;
			}
		} else {
			if (_speed > 0) {
				_da = da1;
			} else {
				_da = da2;
			}
		}
	}
	
}
