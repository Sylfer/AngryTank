package angrygeneration;
/**
* Реализация автомата A3. Общий класс для состояний объекта "Водитель"
*/
public abstract class DriverState {
	/** Имя состояния, используется для протоколирования */
	private String _stateName;
	/** Конструктор, вызываемый подклассами. Делает обязательным указание имени состояния */
	protected DriverState(String aStateName) {
		_stateName = aStateName;
	}
	/** Метод, возвращающий имя состояния */
	public String getName() {
		return _stateName;
	}
	/** Метод, обрабатывающий событие. Каждый из подклассов
	* должен переопределить его в соостветствии с графом переходов
	*/
	public abstract void processEvent(int aEvent, Driver aDriver);
	/** Метод, выполняющий действия при входе в данное состояние. Должен быть
	* переопределен каждым из подклассов в соответствии с графом переходов
	*/
	public abstract void onEnter(Driver aDriver);
	/** Состояние 0 -- траектория "Маятник"*/
	private final static DriverState STATE_0_PENDULUM = new DriverState0();
	/** Состояние 1 -- траектория "Дуга"*/
	private final static DriverState STATE_1_ARC = new DriverState1();
	/** Состояние 2 -- траектория "Уклонение"*/
	private final static DriverState STATE_2_DIGRESSION = new DriverState2();
	/** Состояние 3 -- траектория "Останов" (конец раунда)*/
	private final static DriverState STATE_3_FINISH = new DriverState3();
	/** Состояние 4 -- траектория "Син" (конец раунда)*/
	private final static DriverState STATE_4_SIN = new DriverState4();	
	/** Статический метод, инициализирующий данный управляемый
	* объект (Перевод управляющего автомата в начальное состояние)
	*/
	public static void reset (Driver aDriver) {
		aDriver.setCurrentState(STATE_0_PENDULUM);
	}
	/** Протоколирование для объекта -- начало протоколирования */
	private static void doStartLogging(int aEvent, Driver aDriver) {
	}
	/** Протоколирование для объекта -- конец протоколирования */
	private static void doEndLogging(int aEvent, Driver aDriver) {
	}
	/** Смена состояния автомата, управляющего объектом */
	private static void changeState(DriverState aNewState, Driver aDriver) {
	}
	/**
	* Статический метод, осуществляющий обработку события aEvent объектом aDriver.
	* Сюда также включено все протоколирование
	*/
	public static void processIncomingEvent(int aEvent, Driver aDriver) {
		doStartLogging(aEvent, aDriver);
		// Переход в соответствующее состояние
		aDriver.getCurrentState().processEvent(aEvent, aDriver);
		doEndLogging(aEvent, aDriver);
	}
	/*
	Реализация состояний
	* /
	/** Класс, реализующий состояние 0 автомата "Водитель" */
	private static class DriverState0 extends DriverState {
		public DriverState0() {
			super("State 0");
		}
		// Обработка
		public void processEvent(int aEvent, Driver aDriver) {
			if ((!aDriver.x100_enemyIsNear()) && (!aDriver.x105_wallIsNear())) {
				changeState(STATE_4_SIN, aDriver);
			} else if (aDriver.x110_timeoutExpired()) {
				aDriver.z200_0_initializePendulumTrajectory();
				aDriver.z200_1_randomizePendulumTrajectory();
				aDriver.z200_2_calculatePendulumTrajectory();
			} else {
				aDriver.z200_1_randomizePendulumTrajectory();
				aDriver.z200_2_calculatePendulumTrajectory();
			}
		}
		// Действие
		public void onEnter(Driver aDriver) {
			aDriver.z200_0_initializePendulumTrajectory();
			aDriver.z200_1_randomizePendulumTrajectory();
			aDriver.z200_2_calculatePendulumTrajectory();
		}
	}
	/** Класс, реализующий состояние 1 автомата "Водитель" */
	private static class DriverState1 extends DriverState {
		public DriverState1() {
			super("State 1");
		}
		public void processEvent(int aEvent, Driver aDriver) {
			if (aEvent == Constants.EVENT_HIT_BY_BULLET) {
				changeState(STATE_2_DIGRESSION, aDriver);
			} else if (aDriver.x100_enemyIsNear() || aDriver.x105_wallIsNear() || aEvent ==
				Constants.EVENT_ENEMY_COLLISION || aEvent == Constants.EVENT_WALL_COLLISION) {
				changeState(STATE_0_PENDULUM, aDriver); //Bug found!!!!!!!!!!
			} else if (aDriver.x110_timeoutExpired()) {
				changeState(STATE_3_FINISH, aDriver);
			} else {
				aDriver.z210_1_randomizeArcTrajectory();
				aDriver.z210_2_calculateArcTrajectory();
			}
		}
		public void onEnter(Driver aDriver) {
			aDriver.z210_0_initializeArcTrajectory();
			aDriver.z210_1_randomizeArcTrajectory();
			aDriver.z210_2_calculateArcTrajectory();
		}
	}
	/** Класс, реализующий состояние 2 автомата "Водитель" */
	private static class DriverState2 extends DriverState {
		public DriverState2() {
			super("State 2");
		}
		public void processEvent(int aEvent, Driver aDriver) {
			if (aDriver.x105_wallIsNear() || aDriver.x110_timeoutExpired() || aEvent ==
				Constants.EVENT_ENEMY_COLLISION || aEvent == Constants.EVENT_WALL_COLLISION) {
				changeState(STATE_0_PENDULUM, aDriver);
			} else {
				aDriver.z220_1_randomizeDigressionTrajectory();
				aDriver.z220_2_calculateDigressionTrajectory();
			}
		}
		public void onEnter(Driver aDriver) {
			aDriver.z220_0_initializeDigressionTrajectory();
			aDriver.z220_1_randomizeDigressionTrajectory();
			aDriver.z220_2_calculateDigressionTrajectory();
		}
	}
	/** Класс, реализующий состояние 3 автомата "Водитель" */
	private static class DriverState3 extends DriverState {
		public DriverState3() {
			super("State 3");
		}
		public void processEvent(int aEvent, Driver aDriver) {
			if (aEvent == Constants.EVENT_HIT_BY_BULLET) {
				changeState(STATE_2_DIGRESSION, aDriver);
			} else if (aDriver.x110_timeoutExpired()) {
				changeState(STATE_0_PENDULUM, aDriver);
			} else {
				aDriver.z200_2_calculatePendulumTrajectory();
			}
		}
		public void onEnter(Driver aDriver) {
			aDriver.z230_0_initializeStopTrajectory();
			aDriver.z200_2_calculatePendulumTrajectory();
		}
	}
	/** Состояние 4 -- траектория "Син" (конец раунда)*/
	private static class DriverState4 extends DriverState {
		
		public DriverState4() {
			super("State 4");
		}
		
		public void processEvent(int aEvent, Driver aDriver) {
			
			if (aEvent == Constants.EVENT_HIT_BY_BULLET) {
				changeState(STATE_2_DIGRESSION, aDriver);
			} else if (aDriver.x110_timeoutExpired()) {
				changeState(STATE_0_PENDULUM, aDriver);
			} else {
				aDriver.z200_2_calculatePendulumTrajectory();
			}
			
		}
		
		public void onEnter(Driver aDriver) {
			// Синусовые действия
			aDriver.z240_0_initializeSinTrajectory();
			
		}
	}
}
