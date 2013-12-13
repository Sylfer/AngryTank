package angrygeneration;

import robocode.Event;

public abstract class TargetState {

	/** Имя состояния, используется для протоколирования */
	private String _stateName;
	/** Конструктор, вызываемый подклассами. Делает обязательным указание имени состояния */
	protected TargetState(String aStateName) {
		_stateName = aStateName;
	}
	/** Метод, возвращающий имя состояния */
	public String getName() {
		return _stateName;
	}
	/** Метод, обрабатывающий событие. Каждый из подклассов
	* должен переопределить его в соостветствии с графом переходов
	*/
	public abstract void processEvent(int aEvent, Target aTarget, Event aRobodeEvent);
	/** Метод, выполняющий действия при входе в данное состояние.
	* Должен быть
	* переопределен каждым из подклассов в соответствии с графом переходов
	*/
	public abstract void onEnter(Target aTarget);
	/** Состояние 0 автомата "Цель" */
	private final static TargetState STATE_0 = new TargetState0();
	/** Состояние 1 автомата "Цель" */
	private final static TargetState STATE_1 = new TargetState1();
	/** Состояние 2 автомата "Цель" */
	private final static TargetState STATE_2 = new TargetState2();
	/** Статический метод, инициализирующий данный управляемый
	* объект. (Перевод управляющего автомата в начальное состояние)
	*/
	public static void reset(Target aTarget) {
		aTarget.setCurrentState(STATE_0);
	}
	/** Цель сопровождается*/
	public static boolean targetIsTracked(Target aTarget) {
		return STATE_2 == aTarget.getCurrentState();
	}
	/**
	* Протоколирование для объекта -- начало протоколирования
	*/
	private static void doStartLogging(int aEvent, Target aTarget) {
	}
	/**
	* Протоколирование для объекта -- конец протоколирования
	*/
	private static void doEndLogging(int aEvent, Target aTarget) {
	}
	/**
	* Смена состояния автомата, управляющего объектом
	*/
	private static void changeParentState(TargetState aNewState, Target aTarget) {
		aTarget.setCurrentState(aNewState);
		aNewState.onEnter(aTarget);
	}
	/**
	* Статический метод, осуществляющий обработку события aEvent объектом aTarget,
	* параметр aRobocodeEvent содержит дополнительную информацию о событии.
	* Сюда также включено все протоколирование
	*/
	public static void processIncomingEvent(int aEvent, Target aTarget, Event aRobodeEvent) {
		doStartLogging(aEvent, aTarget);
		aTarget.getCurrentState().processEvent(aEvent, aTarget, aRobodeEvent);
		doEndLogging(aEvent, aTarget);
	}
	/*
	Реализация состояний
	*/
	/** Класс, реализующий состояние 0 автомата "Цель" */
	private static class TargetState0 extends TargetState {
		public TargetState0() {
			super("State 0");
		}
		
		public void processEvent(int aEvent, Target aTarget, Event aRobodeEvent) {
			if (aEvent == Constants.EVENT_UPDATE) {
				aTarget.z1001_updateTargetData(aRobodeEvent);
				changeParentState(STATE_2, aTarget);
			} else if (aEvent == Constants.EVENT_HIT) {
				aTarget.z1010_updateTargetHitStatistics(aRobodeEvent);
				changeParentState(STATE_2, aTarget);
			} else if (aEvent == Constants.EVENT_COLLISION || aEvent == Constants.EVENT_WE_WERE_HIT) {
				changeParentState(STATE_2, aTarget);
			} else if (aEvent == Constants.EVENT_REFRESH && aTarget.x1000_targetDataIsOutOfDate()) {
				changeParentState(STATE_1, aTarget);
			}
		}
		
		public void onEnter(Target aTarget) {
			aTarget.z1000_resetTargetData();
		}
	
	}
	/** Класс, реализующий состояние 1 автомата "Цель" */
	private static class TargetState1 extends TargetState {
		public TargetState1() {
			super("State 1");
		}
		
		public void processEvent(int aEvent, Target aTarget, Event aRobodeEvent) {
			if (aEvent == Constants.EVENT_UPDATE) {
				aTarget.z1001_updateTargetData(aRobodeEvent);
				changeParentState(STATE_2, aTarget);
			} else if (aEvent == Constants.EVENT_HIT) {
				aTarget.z1010_updateTargetHitStatistics(aRobodeEvent);
				changeParentState(STATE_2, aTarget);
			} else if (aEvent == Constants.EVENT_COLLISION || aEvent == Constants.EVENT_WE_WERE_HIT) {
				changeParentState(STATE_2, aTarget);
			} else if (aEvent == Constants.EVENT_START_ROUND) {
				changeParentState(STATE_0, aTarget);
			}
		}
		
		public void onEnter(Target aTarget) {
			aTarget.z1000_resetTargetData();
		}
	}
	/** Класс, реализующий состояние 2 автомата "Цель" */
	private static class TargetState2 extends TargetState {
		public TargetState2() {
			super("State 2");
		}
		
		public void processEvent(int aEvent, Target aTarget, Event aRobodeEvent) {
			if (aEvent == Constants.EVENT_DESTROYED || aEvent == Constants.EVENT_REFRESH &&
			aTarget.x1000_targetDataIsOutOfDate())
				changeParentState(STATE_1, aTarget);
			else if (aEvent == Constants.EVENT_START_ROUND)
				changeParentState(STATE_0, aTarget);
			else if (aEvent == Constants.EVENT_UPDATE) {
				aTarget.z1001_updateTargetData(aRobodeEvent);
			} else if (aEvent == Constants.EVENT_HIT) {
				aTarget.z1010_updateTargetHitStatistics(aRobodeEvent);
			}
		}
		
		public void onEnter(Target aTarget) {
		}
	}	
	
}
