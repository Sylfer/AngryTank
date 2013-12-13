package angrygeneration;

import robocode.Bullet;

public class Gunner {

	/** Направление пушки */
	private double _curHeading;
	/** Текущая температура пушки */
	private double _curGunHeat;
	/** Скорость охлаждения пушки */
	private double _gunHeatDecrement;
	/** Текущая цель*/
	private Target _curTarget;
	/** Текущий прицел*/
	private GeomVektor _curAim;
	/** Текущая мощность выстрела*/
	private double _curFirepower;
	/**На сколько надо повернуть пушку на данном шаге */
	private double _da;
	/**Мощность производимого выстрела */
	private double _firepower;
	/** Объект-супервизор */
	private AngryTankJr _robot;
	/** Конструктор
	* Создает объект "Стрелок" для данного робота-супервизора
	* @param aRobot супервизор
	*/
	Gunner(AngryTankJr aRobot) {
		_robot = aRobot;
		GunnerState.reset(this);
		_gunHeatDecrement = _robot.getGunCoolingRate();
		System.out.println("New: " + _gunHeatDecrement);
		_curAim = new GeomVektor();
	}
	/** Метод, вызываемый в начале каждого раунда */
	public void beginRound() {
		_curHeading = _robot.getGunHeadingRadians();
		_curGunHeat = _robot.getGunHeat();
		_curTarget = null;
		_curAim.reset();
		_robot._lastShotTime = 0;
	}
	/** Начало шага*/
	public void beginTurn() {
		_curHeading = _robot.getGunHeadingRadians();
		_curGunHeat = _robot.getGunHeat();
		_da = 0;
		_firepower = 0;
		GunnerState.processIncomingEvent(10, this);
	}
	/** Конец шага */
	public void endTurn() {
		_robot.setTurnGunRightRadians(_da);
		if (_firepower >= 0.1) {
			Bullet bullet = _robot.fireBullet(_firepower);
			if (bullet != null) {
				_robot._lastShotTime = _robot._currentTime;
				if (_curTarget != null) {
					_curTarget.shoot(_firepower);
				}
			}
		} else {
		_robot.scan();
		}
	}
	/** Вернуть направление пушки */
	public double getCurHeading() {
		return _curHeading;
	}
	/** Текущее состояние*/
	private GunnerState _state;
	/**
	* Получить текущее состояние
	* @return Объект, представляющий текущее состояние
	*/
	public GunnerState getState() {
		return _state;
	}
	/**
	* Установить текущее состояние
	* @param aState новое состояние		
	*/
	public void setState(GunnerState aState) {
		_state = aState;
	}
	/*
	Реализация входных переменных
	*/
	/** x20 : Пушка скоро (в течение трех ходов) охладится */
	public boolean x20_gunIsExpectedToBeCold() {
		boolean result = _curGunHeat / _gunHeatDecrement <= 3;
		return result;
	}
	/** x21 : Пушка охладилась */
	public boolean x21_gunIsCold() {
		boolean result = _curGunHeat <= 0;
		return result;
	}
	/** x22 : До конца охлаждения пушки меньше двух ходов */
	public boolean x22_gunWillBeColdWithinTwoSteps() {
		boolean result = _curGunHeat / _gunHeatDecrement <= 1;
		return result;
	}
	/** x25 : Цель выбрана */
	public boolean x25_targetIsCaptured() {
		boolean result = _curTarget != null;
		return result;
	}
	/** x26 : Цель потеряна */
	public boolean x26_targetIsLost() {
		boolean result = true;
		if (_curTarget != null)
			result = !_curTarget.isTracked();
		return result;
	}
	/** x30 : До конца поворота пушки меньше двух ходов */
	public boolean x30_gunWillTurnWithinTwoSteps() {
		boolean result = true;
		double gun_to_go = AngryTankJr.getAngleDiff(_curHeading, _curAim.getAngle());
		double turn_direction = gun_to_go >= 0 ? 1 : -1;
		double gun_turning_speed = turn_direction * AngryTankJr.MAX_GUN_ROTATION_SPEED
		+ _robot.getDriver().getTurningSpeed();
		
		result = Math.abs(gun_to_go / gun_turning_speed) <= 1;
		return result;
	}
	/** x50 : Наводка правильная*/
	public boolean x50_isPointingFine() {
		boolean result = true;
		double gun_to_go = AngryTankJr.getAngleDiff(_curHeading, _curAim.getAngle());
		result = Math.abs(gun_to_go) < AngryTankJr.PRECISION;
		return result;
	}
	/*
	Реализация выходных воздействий
	*/
	/** z30 : Выбрать цель*/
	public void z30_selectTarget() {
		_curTarget = _robot.getTargets().getClosestTarget(8);
		if (_curTarget == null)
			_curTarget = _robot.getTargets()._closestTarget;
	}
	/** z40 : Рассчитать мощность выстрела */
	public void z40_calculateFirePower() {
		double P = 0.25;
		if (_robot._currentEnegy >= AngryTankJr.ENERGY_NORMAL_THRESHOLD) {
			P = 0.2;
			if (_robot._aliveRobotsCount > 2) {
				P = 0.4;
			}
		} else if (_robot._currentEnegy <= AngryTankJr.ENERGY_WARNING_THRESHOLD) {
			P = 0.25;
			if (_robot._aliveRobotsCount > 2) {
				P = 0.5;
			}
		} else if (_robot._currentEnegy <= AngryTankJr.ENERGY_CRITICAL_THRESHOLD) {
			P = 0.6;
		}
		_curFirepower = _curTarget.getOptimalFirePower(P);
	}
	/** z50_0 : Рассчитать точное упреждение и направить пушку*/
	public void z50_0_calculateFineForestallingAndTurnGun() {
		if (_curTarget != null) {
			GeomVektor predicted_pos;
			predicted_pos = _curTarget.calculateAveragePrediction(
			AngryTankJr.getBulletSpeed(_curFirepower), 2);
			_curAim.setCoords(predicted_pos.getAngle(), predicted_pos.getRadius());
			_da = AngryTankJr.getAngleDiff(_curHeading, predicted_pos.getAngle());
		}
	}
	/** z50_1 : Рассчитать приблизительное упреждение и направить пушку */
	public void z50_1_calculateRoughForestallingAndTurnGun() {
		if (_curTarget != null) {
			GeomVektor predicted_pos;
			predicted_pos = _curTarget.calculateRoughPrediction(
			AngryTankJr.getBulletSpeed(AngryTankJr.BASE_FIRE_POWER), 2);
			_curAim.setCoords(predicted_pos.getAngle(), predicted_pos.getRadius());
			_da = AngryTankJr.getAngleDiff(_curHeading, predicted_pos.getAngle());
		}
	}
	/** z60 : Выстрел */
	public void z60_makeShot() {
		_firepower = _curFirepower;
	}
	/** z70 : Сбросить историю маневрирования цели */
	public void z70_dropTargetPathHistory() {
		if (_curTarget != null)
			_curTarget.resetSpeedHistory();
	}
	/** z80 : Сбросить текущую цель */
	public void z80_dropCurrentTarget() {
		_curTarget = null;
	}
}

