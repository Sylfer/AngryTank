package angrygeneration;

public abstract class RadarState {

	/** Имя состояния, используется для протоколирования */
	private String _stateName;
	/** Конструктор, вызываемый подклассами. Делает обязательным указание имени состояния */
	protected RadarState(String aStateName) {
		_stateName = aStateName;
	}
	/** Метод, возвращающий имя состояния */
	public String getName() {
		return _stateName;
	}
	/** Метод, обрабатывающий событие. Каждый из подклассов
	* должен переопределить его в соостветствии с графом переходов
	*/
	public abstract void processEvent(int aEvent, Radar aRadar);
	/** Метод, выполняющий действия при входе в данное состояние. Должен быть
	* переопределен каждым из подклассов в соответствии с графом переходов
	*/
	public abstract void onEnter(Radar aRadar);
	/** Состояние 0 автомата "Радар" */
	private final static RadarState STATE_0_TURN_LEFT = new RadarState0();
	/** Состояние 1 автомата "Радар" */
	private final static RadarState STATE_1_TURN_RIGHT = new RadarState1();
	/** Состояние 2 автомата "Радар" */
	private final static RadarState STATE_2_TURN_STEP_RIGHT = new RadarState2();
	/** Состояние 3 автомата "Радар" */
	private final static RadarState STATE_3_TURN_STEP_LEFT = new RadarState3();
	/** Статический метод, инициализирующий данный управляемый
	* объект. (Перевод управляющего автомата в начальное состояние)
	*/
	public static void reset(Radar aRadar) {
		aRadar.setCurrentState(STATE_0_TURN_LEFT);
	}
	/**
	* Протоколирование для объекта -- начало протоколирования
	*/
	private static void doStartLogging(int aEvent, Radar aRadar) {
		if (Constants.OBJECTS_LOGGING) {
			Logger.log("Для объекта 'Радар':");
		}
		if (Constants.A4_BEGIN_LOGGING) {
			Logger.logBegin(Constants.RADAR_AUTOMATE_NAME, aRadar.getCurrentState().getName(), aEvent);
		}
	}
	/**
	* Протоколирование для объекта -- конец протоколирования
	*/
	private static void doEndLogging(int aEvent, Radar aRadar) {
		if (Constants.A4_END_LOGGING) {
			Logger.logEnd(Constants.RADAR_AUTOMATE_NAME, aRadar.getCurrentState().getName());
		}
	}
	/**
	* Смена состояния автомата, управляющего объектом
	*/
	private static void changeState(RadarState aNewState, Radar aRadar) {
		if (Constants.A4_TRANS_LOGGING) {
			Logger.logStateChange(Constants.RADAR_AUTOMATE_NAME, aNewState.getName(),
				aRadar.getCurrentState().getName());
		}
		aRadar.setCurrentState(aNewState);
		aNewState.onEnter(aRadar);
	}
	/**
	* Статический метод, осуществляющий обработку события aEvent объектом aRadar.
	* Сюда также включено все протоколирование
	*/
	public static void processIncomingEvent(int aEvent, Radar aRadar) {
		doStartLogging(aEvent, aRadar);
		aRadar.getCurrentState().processEvent(aEvent, aRadar);
		doEndLogging(aEvent, aRadar);
	}
	/*
	Реализация состояний
	*/
	/** Класс, реализующий состояние 0 автомата "Радар" */
	private static class RadarState0 extends RadarState {
		public RadarState0() {
			super("State 0");
		}
		public void processEvent(int aEvent, Radar aRadar) {
			if (aRadar.x70_scanningIsComplete() && aRadar.x80_wholePathIsLesserThan180()) {
				aRadar.z101_0_resetState();
				changeState(STATE_1_TURN_RIGHT, aRadar);
			} else if (aRadar.x70_scanningIsComplete()) {
				changeState(STATE_2_TURN_STEP_RIGHT, aRadar);
			} else {
				aRadar.z100_0_turnLeft();
			}
		}
		public void onEnter(Radar aRadar) {
			aRadar.z100_0_turnLeft();
		}
	}
	/** Класс, реализующий состояние 1 автомата "Радар" */
	private static class RadarState1 extends RadarState {
		public RadarState1() {
			super("State 1");
		}
		public void processEvent(int aEvent, Radar aRadar) {
			if (aRadar.x70_scanningIsComplete() && aRadar.x80_wholePathIsLesserThan180()) {
				aRadar.z101_0_resetState();
				changeState(STATE_0_TURN_LEFT, aRadar);
			} else if (aRadar.x70_scanningIsComplete()) {
				changeState(STATE_3_TURN_STEP_LEFT, aRadar);
			} else {
				aRadar.z100_1_turnRight();
			}
		}
		public void onEnter(Radar aRadar) {
			aRadar.z100_1_turnRight();
		}
	}
	/** Класс, реализующий состояние 2 автомата "Радар" */
	private static class RadarState2 extends RadarState {
		public RadarState2() {
			super("State 2");
		}
		public void processEvent(int aEvent, Radar aRadar) {
			if (aRadar.x70_scanningIsComplete()) {
				aRadar.z101_0_resetState();
			}
			changeState(STATE_0_TURN_LEFT, aRadar);
		}
		public void onEnter(Radar aRadar) {
			aRadar.z101_0_resetState();
			aRadar.z100_1_turnRight();
		}
	}
	/** Класс, реализующий состояние 3 автомата "Радар" */
	private static class RadarState3 extends RadarState {
		public RadarState3() {
			super("State 3");
		}
		public void processEvent(int aEvent, Radar aRadar) {
			if (aRadar.x70_scanningIsComplete()) {
				aRadar.z101_0_resetState();
			}
			changeState(STATE_1_TURN_RIGHT, aRadar);
		}
		public void onEnter(Radar aRadar) {
			aRadar.z101_0_resetState();
			aRadar.z100_0_turnLeft();
		}
	}	
	
}
