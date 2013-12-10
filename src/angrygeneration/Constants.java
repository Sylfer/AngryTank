package angrygeneration;

public class Constants {
	
	/*
	Константы для событий
	*/
	public static final int EVENT_ROUND_START = 9;
	public static final int EVENT_STEP_START = 10;
	public final static int EVENT_DEATH = 20;
	public static final int EVENT_WIN = 21;
	public static final int EVENT_ENEMY_COLLISION = 40;
	public static final int EVENT_WALL_COLLISION = 41;
	public static final int EVENT_HIT_BY_BULLET = 45;
	public static final int EVENT_UPDATE = 100;
	public static final int EVENT_DESTROYED = 110;
	public static final int EVENT_START_ROUND = 120;
	public static final int EVENT_REFRESH = 140;
	public static final int EVENT_HIT = 130;
	public static final int EVENT_WE_WERE_HIT = 136;
	public static final int EVENT_COLLISION = 135;
	/*
	Имена автоматов
	*/
	/** Имя автомата A0 (Супревизор) */
	public static final String SUPERVISOR_AUTOMATE_NAME = "A0(Supervisor)";
	/** Имя автомата A1 (Стрелок) */
	public static final String GUNNER_AUTOMATE_NAME = "A1(Gunner)";
	/** Имя автомата A3 (Водитель) */
	public final static String DRIVER_AUTOMATE_NAME = "A3(Driver)";
	/** Имя автомата A4 (Радар) */
	public final static String RADAR_AUTOMATE_NAME = "A4(Radar)";
	/** Имя автомата A5 (Цель) */
	public final static String TARGET_AUTOMATE_NAME = "A5(Target)";
	
	/*
	Параметры протоколирования
	*/
	/** Протоколирование включено */
	public static final boolean ANY_DEBUG = false;
	/** Протоколирование в файл включено */
	public static final boolean TO_LOG_FILE = true && ANY_DEBUG;
	/** Консольное протоколирование включено */
	public static final boolean TO_CONSOLE = false && ANY_DEBUG;
	/** Протоколирование отладочной информации включено */
	public static final boolean SWITCH_DEBUG = true;
	/** Протоколирование входных воздействий включено */
	public static final boolean INPUTS_LOGGING = true && SWITCH_DEBUG && ANY_DEBUG;
	/** Протоколирование выходных воздействий включено */
	public static final boolean OUTPUTS_LOGGING = true && SWITCH_DEBUG && ANY_DEBUG;
	/** Протоколирование имен объектов включено */
	public static final boolean OBJECTS_LOGGING = true && ANY_DEBUG;
	/*
	Протоколирование для автомата A0
	*/
	public static final boolean A0_LOGGING = true && SWITCH_DEBUG && ANY_DEBUG;
	public static final boolean A0_BEGIN_LOGGING = true && A0_LOGGING && ANY_DEBUG;
	public static final boolean A0_END_LOGGING = true && A0_LOGGING && ANY_DEBUG;
	/*
	Протоколирование для автомата A1
	*/
	public static final boolean A1_LOGGING = true && SWITCH_DEBUG && ANY_DEBUG;
	public static final boolean A1_BEGIN_LOGGING = true && A1_LOGGING && ANY_DEBUG;
	public static final boolean A1_END_LOGGING = true && A1_LOGGING && ANY_DEBUG;
	/*
	Протоколирование для автомата A3
	*/
	public static final boolean A3_LOGGING = true && SWITCH_DEBUG && ANY_DEBUG;
	public static final boolean A3_BEGIN_LOGGING = true && A3_LOGGING && ANY_DEBUG;
	public static final boolean A3_END_LOGGING = true && A3_LOGGING && ANY_DEBUG;
	public static final boolean A3_TRANS_LOGGING = true && A3_LOGGING && ANY_DEBUG;
	/*
	Протоколирование для автомата A4
	*/
	public static final boolean A4_LOGGING = true && SWITCH_DEBUG && ANY_DEBUG;
	public static final boolean A4_BEGIN_LOGGING = true && A4_LOGGING && ANY_DEBUG;
	public static final boolean A4_END_LOGGING = true && A4_LOGGING && ANY_DEBUG;
	public static final boolean A4_TRANS_LOGGING = true && A4_LOGGING && ANY_DEBUG;
	/*
	Протоколирование для автомата A5
	*/
	public static final boolean A5_LOGGING = true && SWITCH_DEBUG && Constants.ANY_DEBUG;
	public static final boolean A5_BEGIN_LOGGING = true && A5_LOGGING && Constants.ANY_DEBUG;
	public static final boolean A5_END_LOGGING = true && A5_LOGGING && Constants.ANY_DEBUG;
	public static final boolean A5_TRANS_LOGGING = true && A5_LOGGING && Constants.ANY_DEBUG;	

}
