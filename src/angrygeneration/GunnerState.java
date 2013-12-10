package angrygeneration;
/**
* Реализация автомата A1. Общий класс для состояний объекта "Стрелок"
*/
public abstract class GunnerState {
	/** Имя состояния, используется для протоколирования */
	private String _name;
	/** Конструктор, вызываемый подклассами.
	* Делает обязательным указание имени состояния */
	protected GunnerState(String aName) {
		_name = aName;
	}
	/** Метод, возвращающий имя состояния */
	public String getName() {
		return _name;
	}
	/** Метод, обрабатывающий событие. Каждый из подклассов
	* должен переопределить его в соостветствии с графом переходов
	*/
	public abstract void processEvent(int aEvent, Gunner aGunMaster);
	/** Метод, выполняющий действия при входе в данное состояние. Должен быть
	* переопределен каждым из подклассов в соответствии с графом переходов
	*/
	public abstract void onEnter(Gunner aGunMaster);
	/** Статический метод, инициализирующий данный управляемый
	* объект. (Перевод управляющего автомата в начальное состояние)
	*/
	public static void reset(Gunner aGunMaster) {
	aGunMaster.setState(STATE_0);
	}
	/**
	* Смена состояния автомата, управляющего объектом
	*/
	protected static void chageParentState(Gunner aGunMaster, GunnerState aNewState) {
		Logger.logStateChange(Constants.GUNNER_AUTOMATE_NAME, aNewState.getName(),
		aGunMaster.getState().getName());
		aGunMaster.setState(aNewState);
		aNewState.onEnter(aGunMaster);
	}
	/** Состояние 0 автомата "Стрелок" */
	private static GunnerState STATE_0 = new GunnerState_0();
	/** Состояние 1 автомата "Стрелок" */
	private static GunnerState STATE_1 = new GunnerState_1();
	/** Состояние 2 автомата "Стрелок" */
	private static GunnerState STATE_2 = new GunnerState_2();
	/** Состояние 3 автомата "Стрелок" */
	private static GunnerState STATE_3 = new GunnerState_3();
	/**
	* Статический метод, осуществляющий обработку события aEvent объектом aGunMaster.
	* Сюда также включено все протоколирование
	*/
	public static void processIncomingEvent(int aEvent, Gunner aGunMaster) {
		doStartLogging(aGunMaster, aEvent);
		aGunMaster.getState().processEvent(aEvent, aGunMaster);
		doEndLogging(aGunMaster);
	}
	/**
	* Протоколирование для объекта -- конец протоколирования
	*/
	private static void doEndLogging(Gunner aGunMaster) {
		if (Constants.A1_END_LOGGING)
			Logger.logEnd(Constants.GUNNER_AUTOMATE_NAME, aGunMaster.getState().getName());
	}
	/**
	* Протоколирование для объекта -- начало протоколирования
	*/
	private static void doStartLogging(Gunner aGunMaster, int aEvent) {
		if (Constants.OBJECTS_LOGGING)
			Logger.log("Для объекта 'Стрелок':");
		if (Constants.A1_BEGIN_LOGGING)
			Logger.logBegin(Constants.GUNNER_AUTOMATE_NAME, aGunMaster.getState().getName(), aEvent);
	}
	/*
	Реализация состояний
	* /
	/** Класс, реализующий состояние 0 автомата "Стрелок" */
	private static class GunnerState_0 extends GunnerState {
		public GunnerState_0() {
			super("State 0");
		}
		public void processEvent(int aEvent, Gunner aGunMaster) {
			if (aGunMaster.x25_targetIsCaptured()) {
				chageParentState(aGunMaster, STATE_3);
			} else {
				aGunMaster.z30_selectTarget();
				aGunMaster.z70_dropTargetPathHistory();
				aGunMaster.z50_1_calculateRoughForestallingAndTurnGun();
			}
		}
		public void onEnter(Gunner aGunMaster) {
			aGunMaster.z30_selectTarget();
			aGunMaster.z70_dropTargetPathHistory();
		}
	}
	/** Класс, реализующий состояние 1 автомата "Стрелок" */
	private static class GunnerState_1 extends GunnerState {
		public GunnerState_1() {
			super("State 1");
		}
		public void processEvent(int aEvent, Gunner aGunMaster) {
			if (aGunMaster.x26_targetIsLost()) {
				aGunMaster.z80_dropCurrentTarget();
				chageParentState(aGunMaster, STATE_0);
			} else if (aGunMaster.x30_gunWillTurnWithinTwoSteps() &&
				aGunMaster.x22_gunWillBeColdWithinTwoSteps()) {
				chageParentState(aGunMaster, STATE_2);
			} else {
				aGunMaster.z40_calculateFirePower();
				aGunMaster.z50_0_calculateFineForestallingAndTurnGun();
			}
		}
		public void onEnter(Gunner aGunMaster) {
			aGunMaster.z40_calculateFirePower();
			aGunMaster.z50_0_calculateFineForestallingAndTurnGun();
		}
	}
	/** Класс, реализующий состояние 2 автомата "Стрелок" */
	private static class GunnerState_2 extends GunnerState {
		public GunnerState_2() {
			super("State 2");
		}
		public void processEvent(int aEvent, Gunner aGunMaster) {
			if (aGunMaster.x26_targetIsLost()) {
				aGunMaster.z80_dropCurrentTarget();
				chageParentState(aGunMaster, STATE_0);
			} else if (aGunMaster.x21_gunIsCold() && aGunMaster.x50_isPointingFine()) {
				aGunMaster.z60_makeShot();
				chageParentState(aGunMaster, STATE_0);
			} else if (!aGunMaster.x30_gunWillTurnWithinTwoSteps()) {
				chageParentState(aGunMaster, STATE_1);
			} else {
				aGunMaster.z40_calculateFirePower();
				aGunMaster.z50_0_calculateFineForestallingAndTurnGun();
			}
		}
		public void onEnter(Gunner aGunMaster) {
			aGunMaster.z40_calculateFirePower();
			aGunMaster.z50_0_calculateFineForestallingAndTurnGun();
		}
	}
	/** Класс, реализующий состояние 3 автомата "Стрелок" */
	private static class GunnerState_3 extends GunnerState {
		public GunnerState_3() {
			super("State 3");
		}
		public void processEvent(int aEvent, Gunner aGunMaster) {
			if (aGunMaster.x26_targetIsLost()) {
				aGunMaster.z80_dropCurrentTarget();
				chageParentState(aGunMaster, STATE_0);
			} else if (aGunMaster.x20_gunIsExpectedToBeCold()) {
				chageParentState(aGunMaster, STATE_1);
			} else {
				aGunMaster.z50_1_calculateRoughForestallingAndTurnGun();
			}
		}
		public void onEnter(Gunner aGunMaster) {
			aGunMaster.z50_1_calculateRoughForestallingAndTurnGun();
		}
	}
}