package angrygeneration;

public abstract class SuperVisorState {

	/** Имя состояния, используется для протоколирования */
	private String _stateName;
	
	/** Метод, возвращающий имя состояния */
	public String getName() {
	return _stateName;
	}
	/** Конструктор, вызываемый подклассами. Делает обязательным указание имени состояния */
	public SuperVisorState(String aStateName) {
	_stateName = aStateName;
	}
	/**
	* Смена состояния автомата, управляющего объектом
	*/
	protected void chageParentState(AngryTankJr aRobot, SuperVisorState aNewState) {
	changeParentState(aRobot, aNewState);
	aNewState.onEnter(aRobot);
	}
	
	/**
	* Протоколирование для объекта -- начало протоколирования
	*/
	private static void doStartLogging(int aEvent, AngryTankJr aRobot) {
	};
	/**
	* Протоколирование для объекта -- конец протоколирования
	*/
	private static void doEndLoggint(int aEvent, AngryTankJr aRobot) {
	};	
	
	/** Метод, обрабатывающий события. Каждый из подклассов
	* должен переопределить его в соостветствии с графом переходов
	*/
	public abstract void processEvent(AngryTankJr aRobot, int aEvent);
	/** Метод, выполняющий действия при входе в данное состояние. Должен быть
	* переопределен каждым из подклассов в соответствии с графом переходов
	*/
	public abstract void onEnter(AngryTankJr aRobot);
	/** Состояние 0 автомата "Супервизор" */
	private static SuperVisorState STATE_0 = new SuperVisorState0();
	/** Состояние 1 автомата "Супервизор" */
	private static SuperVisorState STATE_1 = new SuperVisorState1();
	/** Состояние 2 автомата "Супервизор" */
	private static SuperVisorState STATE_2 = new SuperVisorState2();
	/** Статический метод, инициализирующий данный управляемый
	* объект. Перевод управляющего автомата в начальное состояние
	*/
	public static void reset(AngryTankJr aRobot) {
		aRobot.setCurrentState(STATE_0);
	}
	/**
	* Статический метод, осуществляющий обработку события aEvent объектом aRobot.
	* Сюда также включено все протоколирование
	*/
	public static void processIncomingEvent(int aEvent, AngryTankJr aRobot) {
		aRobot.getCurrentState().processEvent(aRobot, aEvent);
	}
	/**
	* Смена состояния автомата, управляющего объектом
	*/
	public static void changeParentState(AngryTankJr aRobot, SuperVisorState aState) {
		aRobot.setCurrentState(aState);
	}
	/*
	Реализация состояний
	*/
	/** Класс, реализующий состояние 0 автомата "Супервизор" */
	private static class SuperVisorState0 extends SuperVisorState {
		public SuperVisorState0() {
			super("State 0");
		}
		
		public void processEvent(AngryTankJr aRobot, int aEvent) {
			if (aEvent == Constants.EVENT_ROUND_START) {
				aRobot.z10_0_initializeAtStart();
				chageParentState(aRobot, STATE_1);
			}
		}
		
		public void onEnter(AngryTankJr aRobot) {
		}
	}
	
	/** Класс, реализующий состояние 1 автомата "Супервизор" */
	private static class SuperVisorState1 extends SuperVisorState {
		public SuperVisorState1() {
			super("State 1");
		}
		
		public void processEvent(AngryTankJr aRobot, int aEvent) {
			if (aEvent == Constants.EVENT_DEATH || aEvent == Constants.EVENT_WIN) {
				chageParentState(aRobot, STATE_2);
			}
			else if (aEvent == Constants.EVENT_STEP_START) {
				aRobot.z10_2_requestInputParametersAtNewStep();
			}
		}
		
		public void onEnter(AngryTankJr aRobot) {
			aRobot.z10_1_initializeAtNewRound();
			aRobot.z10_2_requestInputParametersAtNewStep();
		}
	}
	
	/** Класс, реализующий состояние 2 автомата "Супервизор" */
	private static class SuperVisorState2 extends SuperVisorState {
		public SuperVisorState2() {
			super("State 2");
		}
		
		public void processEvent(AngryTankJr aRobot, int aEvent) {
			if (aEvent == Constants.EVENT_ROUND_START) {
			chageParentState(aRobot, STATE_1);
			}
		}

		public void onEnter(AngryTankJr aRobot) {
			aRobot.z20_printRoundStatistics();
		}
		
	}
}
