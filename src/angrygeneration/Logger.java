package angrygeneration;

import java.io.PrintStream;

public class Logger {
	
	/** Выводной поток для протоколирования*/
	public static PrintStream _out;
	/** Запротоколировать сообщение
	* @param aMessage сообщение
	*/
	public static void log(String aMessage) {
		if (Constants.TO_CONSOLE) {
			_out.println(aMessage);
		}
		if (Constants.TO_LOG_FILE) {
			System.out.println(aMessage);
		}
	}
	/**
	* Запротоколировать сообщение, используя данный тип скобок
	* @param aMessage сообщение
	* @param aBracket скобка
	*/
	public static void log(String aMessage, String aBracket) {
		String out_str = aBracket + " " + aMessage;
		if (Constants.TO_CONSOLE) {
			_out.println(out_str);
		}
		if (Constants.TO_LOG_FILE) {
			System.out.println(out_str);
		}
	}
	/**
	* Протоколирование начала работы автомата
	* @param aAutomate имя автомата
	* @param aStateName имя сотояния
	* @param aEvent событие
	*/
	public static void logBegin(String aAutomate, String aStateName, int aEvent) {
			log(aAutomate + ": Автомат " + aAutomate
			+ " запущен в состоянии " + aStateName
			+ " с событием e" + aEvent, "{");
	}
	/**
	* Протоколирование окончания работы автомата
	* @param aAutomate имя автомата
	* @param aState имя конечного состояния автомата
	*/
	public static void logEnd(String aAutomate, String aState) {
		log(aAutomate + ": Автомат " + aAutomate + " завершил свою работу в состоянии " + aState, "}");
	}
	/**
	* Протоколирование изменения состояния автомата
	* @param aAutomate има автомата
	* @param aNewState имя исходного состояния
	* @param aOldState имя нового состояния
	*/
	public static void logStateChange(String aAutomate, String aNewState, String aOldState) {
		log(aAutomate + ": Автомат " + aAutomate
		+ " перешел из состояния " + aOldState
		+ " в состояние " + aNewState, " T");
	}
	/**
	* Протоколирование значений входных переменных
	* @param x_name имя входной переменной
	* @param comment коментарий
	* @param result значение переменной
	*/
	public static void logInput(String x_name, String comment, boolean result) {
		String res_str = result ? "ДА" : "НЕТ";
		log(x_name + ": " + comment + "? - " + res_str + ".", " i");
	}
	/**
	* Протоколирование значений выходных переменных
	* @param z_name имя выходной переменной
	* @param comment комментарии
	*/
	public static void logOutput(String z_name, String comment) {
		log(z_name + ": " + comment + ".", " *");
	}
}
