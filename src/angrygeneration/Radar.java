package angrygeneration;
/**
* Класс “Радар”
*/
public class Radar {
	/** Направление радара */
	private double _oldHeading;
	/** Направление радара */
	private double _currentHeading;
	/**Угол, на который надо повернуть радар на следующем шаге */
	private double _deltaAngle;
	/** Пройденный радаром путь*/
	private double _radarPath;
	/** Текущее состояние*/
	private RadarState _currentState;
	/** Объект-супервизор */
	private AngryTankJr _robot;
	/**
	* Получить текущее состояние
	* @return Объект, представляющий текущее состояние
	*/
	public RadarState getCurrentState() {
		 return _currentState;
	}
	/**
	* Установить текущее состояние
	* @param aCurrentState новое состояние
	*/
	public void setCurrentState(RadarState aCurrentState) {
		_currentState = aCurrentState;
	}
	/**
	* Создает объект "Радар" для данного робота-супервизора
	* @param aRobot супервизор
	*/
	public Radar(AngryTankJr aRobot) {
		_robot = aRobot;
		RadarState.reset(this);
	}
	/** Метод, вызываемый в начале каждого раунда */
	public void beginRound() {
		_oldHeading = _robot.getRadarHeadingRadians();
		_currentHeading = _robot.getRadarHeadingRadians();
		_deltaAngle = 0;
		_radarPath = 0;
	}
	/** Метод, вызываемый в начале каждого шага */
	public void beginTurn() {
		_oldHeading = _currentHeading;
		_currentHeading = _robot.getRadarHeadingRadians();
		_radarPath = _radarPath + AngryTankJr.getAngleDiff(_oldHeading, _currentHeading);
		RadarState.processIncomingEvent(10, this);
	}
	/** Метод, вызываемый в конце шага */
	public void endTurn() {
		_robot.setTurnRadarRightRadians(_deltaAngle);
	}
	/*
	Реализация входных переменных
	*/
	/* x70 : Цикл сканирования завершен */
	public boolean x70_scanningIsComplete() {
		boolean result = _robot.getTargets().scanCompleted();
		return result;
	}
	/* x80 : Пройденный радаром путь меньше 180 градусов */
	public boolean x80_wholePathIsLesserThan180() {
		boolean result = Math.abs(_radarPath) < Math.PI;
		return result;
	}
	/*
	Реализация выходных воздействий
	*/
	/* z100_0 : Повернуть радар влево */
	public void z100_0_turnLeft() {
		_deltaAngle = -1000;
	}
	/** z100_1 : Повернуть радар вправо */
	public void z100_1_turnRight() {
		_deltaAngle = 1000;
	}
	/** z101_0 : Сбросить память пройденного радаром пути */
	public void z101_0_resetState() {
		_radarPath = 0;
	}
}