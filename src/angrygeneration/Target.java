package angrygeneration;

import robocode.Event;
import robocode.ScannedRobotEvent;

public class Target {


private static final double LONG_RANGE_DISTANCE = 650;
private static final double CLOSE_RANGE_DISTANCE = 150;
private static final double POINT_BLANK_RANGE = 70;
/** Количество раундов, через которое
* информация считается устаревшей
*/
private static final long ROUNDS_TO_MAKE_INFO_INVALID = 20;
private static final double INITILAL_PROBABILITY = 1.2;
private static final double _Tbase = 3;
/*
Переменные объекта
*/
/** Имя цели*/
private String _name;
/** Пеленг*/
public double _angle;
/**Дистанция */
public double _distance;
/**Линейная скорость */
private double _velocity;
/** Усредненная линейная скорость */
private double _averageVelocity;
/**Скорость поворота */
private double _angularVelocity;
/** Усредненная скорость поворота*/
private double _averageAngularVelocity;
/** Произошло стколкновение */
private boolean _collisionDetected;
/** Направление движения цели при последнем измерении*/
private double _recentTargetHeading;
/** Направление движения цели при предпоследнем измерении*/
private double _oldTargetHeading;
/** Время последнего измерения параметров цели*/
private long _lastUpdateTime;
/**Упреждающий вектор от текущего положения нашего робота */
private GeomVektor _predictionVector = new GeomVektor();
/** Энергия цели */
private double _TargetEnergy;
/** Количество попаданий в цель*/
private double _hits;
/** Количество выстрелов в цель */
private double _shots;
/** Базовая вероятность */
private double _baseProbability;
/** Усреденное время */
private double _Taverage;
/** Робот-супервизор*/
private AngryTankJr _AngryTankJr;
/** Текущее состояние */
private TargetState _currentState;
/**
* Получить текущее состояние
* @return Объект, представляющий текущее состояние
*/
public TargetState getCurrentState() {
	return _currentState;
}
/**
* Установить текущее состояние
* @param aNewState новое состояние
*/
public void setCurrentState(TargetState aNewState) {
	_currentState = aNewState;
}
/** Конструктор */
Target(AngryTankJr aAngryTankJr, ScannedRobotEvent e) {
	_AngryTankJr = aAngryTankJr;
	_name = e.getName();
	_angle = AngryTankJr.normalizeAngle(_AngryTankJr.getHeadingRadians()
	+ e.getBearingRadians());
	_distance = e.getDistance();
	_averageVelocity = _velocity = e.getVelocity();
	_averageAngularVelocity = 0;
	_angularVelocity = 0;
	_oldTargetHeading = _recentTargetHeading = e.getHeadingRadians();
	_lastUpdateTime = _AngryTankJr._currentTime;
	_hits = 0;
	_shots = 0;
	_Taverage = 3;
	_baseProbability = 1;
	TargetState.reset(this);
	TargetState.processIncomingEvent(Constants.EVENT_UPDATE, this, e);
}
/** Начало раунда */
public void beginRound() {
	TargetState.processIncomingEvent(Constants.EVENT_START_ROUND, this, null);
}
/** Обновление состояния цели */
public void targetRefresh() {
	TargetState.processIncomingEvent(Constants.EVENT_REFRESH, this, null);
}
/** Обновление цели */
public void targetUpdate(ScannedRobotEvent aEvent) {
	TargetState.processIncomingEvent(Constants.EVENT_UPDATE, this, aEvent);
}
/** Уничтожение цели*/
public void targetDestroyed() {
	TargetState.processIncomingEvent(Constants.EVENT_DESTROYED, this, null);
}
/** Попадание в цель*/
public void targetHit() {
	TargetState.processIncomingEvent(Constants.EVENT_HIT, this, null);
}
/** Столкновение с целью */
public void collision() {
	TargetState.processIncomingEvent(Constants.EVENT_COLLISION, this, null);
}
/** Попадание в нас */
public void hitByBullet() {
	TargetState.processIncomingEvent(Constants.EVENT_WE_WERE_HIT, this, null);
}
/** Выстрел по цели */
public void shoot(double firepower) {
	double T = _predictionVector.getRadius() / AngryTankJr.getBulletSpeed(firepower);
	T = Math.max(T, 1);
	double P = INITILAL_PROBABILITY - T * (INITILAL_PROBABILITY - _baseProbability) / _Tbase;
	P = Math.min(P, INITILAL_PROBABILITY);
	P = Math.max(P, 0);
	P = P / 1.2;
	_baseProbability = INITILAL_PROBABILITY - _Tbase * (INITILAL_PROBABILITY - P) / T;
	_baseProbability = Math.min(_baseProbability, INITILAL_PROBABILITY);
	_baseProbability = Math.max(_baseProbability,0);
	_Taverage = (_Taverage + T) / 2;
	_shots++;
}
/** Сброс статистики скорости */
public void resetSpeedHistory() {
	_averageAngularVelocity = _angularVelocity;
	_averageVelocity = _velocity;
}
/** Цель сопровождается */
public boolean isTracked() {
	return TargetState.targetIsTracked(this);
}
/** Была ли цель обновлена после указанного времени */
public boolean wasUpdatedAfter(long scan_time) {
	return _lastUpdateTime > scan_time;
}
/** Вернуть оптимальную мощность выстрела по заданной цели */
public double getOptimalFirePower(double P) {
	double R = _predictionVector.getRadius();
	double S = (INITILAL_PROBABILITY - _baseProbability) * R / ((INITILAL_PROBABILITY - P) * _Tbase);
	double resultFirePower = (20 - S) / 3.0;
	resultFirePower = Math.min(resultFirePower,AngryTankJr.MAX_FIRE_POWER );
	double damage;
	if (resultFirePower > 1) {
		damage = resultFirePower * 3 + (2 * (resultFirePower - 1) );
	} else {
		damage = resultFirePower * 3 ;
	}
	if (damage > _TargetEnergy) {
		if (_TargetEnergy < 3) {
			resultFirePower = _TargetEnergy / 3.0;
			resultFirePower = resultFirePower < 0.1 ? 0.1 : resultFirePower;
		} else {
			resultFirePower = (_TargetEnergy + 2.0) / 5.0;
		}
	}
	if (resultFirePower < 0.1) {
		if ((R < LONG_RANGE_DISTANCE) && (_AngryTankJr._currentEnegy >
		AngryTankJr.ENERGY_WARNING_THRESHOLD)) {
			resultFirePower = 0.1;
		} else if ((_TargetEnergy > _AngryTankJr._currentEnegy)
		&& ((_AngryTankJr._currentTime - _AngryTankJr._lastShotTime) > AngryTankJr.FIRE_DELAY_CRITICAL)) {
			resultFirePower = 0.1;
		}
	} else {
		if (_AngryTankJr._currentEnegy < AngryTankJr.ENERGY_CRITICAL_THRESHOLD) {
			resultFirePower = Math.min(resultFirePower,_AngryTankJr._currentEnegy / 5);
			resultFirePower = Math.max(resultFirePower, 0.1);
			if (_AngryTankJr._currentEnegy < 0.2) {
				resultFirePower = 0;
			}
		}
	}
	return resultFirePower;
}
/** Рассчитать грубое упреждение */
public GeomVektor calculateRoughPrediction(double aBuletSpeed, double aPredictionGap) {
	double curBulletTime;
	curBulletTime = aPredictionGap + _distance / aBuletSpeed;
	_predictionVector.setCoords(_angle, _distance);
	if (_distance > CLOSE_RANGE_DISTANCE) {
		if (Math.abs(_velocity) > AngryTankJr.PRECISION) {
			_predictionVector.add(get_path(_averageVelocity, _averageAngularVelocity,
			_recentTargetHeading, curBulletTime));
		}
	}
	return _predictionVector;
}
/** Рассчитать усредненное упреждение */
public GeomVektor calculateAveragePrediction(double aBulletSpeed, double aPredictionGap) {
	predictPosition(_averageVelocity, _averageAngularVelocity, aBulletSpeed, aPredictionGap);
	return _predictionVector;
}
/** Вывести статистику */
public void logStatisitics() {
	Logger.log("---- Статистика для " + _name + " ----");
	Logger.log("Выстрелов: " + _shots + ", попаданий: " + _hits);
	Logger.log("Вероятность: " + _hits / _shots + ", базовая: " + _baseProbability);
	Logger.log("---------");
}
/*
Реализация входных переменных
*/
/** x1000 : Информация о цели устарела*/
public boolean x1000_targetDataIsOutOfDate() {
	boolean result = (_AngryTankJr._currentTime - _lastUpdateTime) > ROUNDS_TO_MAKE_INFO_INVALID;
	if (Constants.INPUTS_LOGGING) {
		Logger.logInput("x1000", "Информация о цели устарела", result);
	}
	return result;
}
/*
Реализация выходных воздействий
*/
/** z1000 : Сбросить параметры цели*/
public void z1000_resetTargetData() {
	if (Constants.OUTPUTS_LOGGING) {
		Logger.logOutput("z1000", "Сбросить параметры цели");
	}
	_averageAngularVelocity = 0;
	_angularVelocity = 0;
	_averageVelocity = _velocity = 0;
	_lastUpdateTime = 0;
	_predictionVector.reset();
}
/** z1001 : Обновить параметры цели*/
public void z1001_updateTargetData(Event event) {
	if (Constants.OUTPUTS_LOGGING) {
		Logger.logOutput("z1001", "Обновить параметры цели");
	}
	if (_lastUpdateTime != _AngryTankJr._currentTime) // Защита от двойного вызова на одном шаге
	{
		ScannedRobotEvent e = (ScannedRobotEvent) event;
		_TargetEnergy = e.getEnergy();
		_distance = Math.abs(e.getDistance());
		_angle = AngryTankJr.normalizeAngle(_AngryTankJr.getHeadingRadians() + e.getBearingRadians());
		_velocity = e.getVelocity();
		_averageVelocity = (_velocity + _averageVelocity) / 2.0;
		_oldTargetHeading = _recentTargetHeading;
		_recentTargetHeading = AngryTankJr.normalizeAngle(e.getHeadingRadians());
		_angularVelocity = AngryTankJr.getAngleDiff(_oldTargetHeading, _recentTargetHeading)
		/ (_AngryTankJr._currentTime - _lastUpdateTime);
		_averageAngularVelocity = (_angularVelocity + _averageAngularVelocity) / 2.0;
		_lastUpdateTime = _AngryTankJr._currentTime;
	}
}
/** z1010 : Обновить статистику попаданий в цель*/
public void z1010_updateTargetHitStatistics(Event e) {
	if (Constants.OUTPUTS_LOGGING) {
		Logger.logOutput("z1010", "Обновить статистику попаданий в цель");
	}
	double P = INITILAL_PROBABILITY - _Taverage * (INITILAL_PROBABILITY - _baseProbability) / _Tbase;
	P = P * 1.4;
	P = Math.min(P,INITILAL_PROBABILITY);
	P = Math.max(P,0);
	_baseProbability = INITILAL_PROBABILITY - _Tbase * (INITILAL_PROBABILITY - P) / _Taverage;
	_baseProbability = Math.min(_baseProbability, INITILAL_PROBABILITY);
	_baseProbability = Math.max(_baseProbability,0);
	_hits++;
}
/*
Вспомогательные методы
*/
/** Рассчитать упреждение с учетом:
* <ul>
* <li>отличия во времени подлета пули;
* <li>того, что выстрел будет с задержкой на prediction_gap
* </ul>
*/
private void predictPosition(double v, double w,
double bullet_speed, double prediction_gap) {
	final int maximum_iterations = 5;
	double old_bullet_time, cur_bullet_time;
	int i = 0;
	GeomVektor cur_prediction = new GeomVektor();
	if (_distance < POINT_BLANK_RANGE) {
		cur_prediction.setCoords(_angle, _distance);
	} else if (Math.abs(v) > AngryTankJr.PRECISION) {
	old_bullet_time = cur_bullet_time = prediction_gap + _distance / bullet_speed;
	_collisionDetected = false;
	cur_prediction = get_predicted_point(v, w, cur_bullet_time, true);
		if (!_collisionDetected)
			for (i = 0; i < maximum_iterations; i++) {
				old_bullet_time = cur_bullet_time;
				cur_bullet_time = prediction_gap + cur_prediction.getRadius() / bullet_speed;
				if (Math.abs(cur_bullet_time - old_bullet_time) < 0.5) break;
					cur_prediction = get_predicted_point(v, w, cur_bullet_time, false);
			}
		} else {
		cur_prediction.setCoords(_angle, _distance);
		}
		_predictionVector.setCoords(cur_prediction.getAngle(), cur_prediction.getRadius());
}
/** Расчитать участок траектории с неизменной скоростью */
private GeomVektor get_predicted_point(double v, double w,
double t, boolean check_collisions) {
	GeomVektor cur_prediction = new GeomVektor(_angle, _distance);
	// Считаем от нашего танка, так как надо получить
	// абсолютные координаты для проверки вылета
	double time_discret = 5;
	if (check_collisions) {
		// Убогий (по реккурентной формуле) способ подсчета точки
		// столкновения цели со стеной.
		// Разбиваем весь участок траектории на куски и
		// для каждого куска проверяем выход за границу.
		// Если выход за границу произошел - определяем
		// точный момент выхода и считаем заново путь,
		// проделанный от исходной точки до рассчитанного момента.
		for (double i = 0; i * time_discret < t; i++) {
			double time_delta = Math.min(time_discret,
			t - i * time_discret);
			cur_prediction.add(get_path(v, w, _recentTargetHeading, time_delta));
			if (vector_out_of_field(cur_prediction)) {
				// Цель вылетела
				// Момент времени, в который это произошло,
				// лежит в интервале [i*time_discret ; i*time_discret + time_delta]
				// Дальше считать по шагам
				_collisionDetected = true;
				cur_prediction.setCoords(_angle, _distance);
				cur_prediction.add(get_path(v, w, _recentTargetHeading, i * time_discret));
			for (int j = 1; j <= time_delta; j++) {
				cur_prediction.add(get_path(v, w, _recentTargetHeading, 1));
				if (vector_out_of_field(cur_prediction)) {
					// Определили точный момент выхода цели за границу поля.
					// Он равен i*time_discret + j
					// Таким образом, надо сосчитать путь за это время
					cur_prediction.setCoords(_angle, _distance);
					cur_prediction.add(
					get_path(v, w, _recentTargetHeading, i * time_discret + j));
					return cur_prediction;
				}
			}
			// Если оказались здесь, значит цель почему-то не доехала до края
			// за время, которое рассчитывали по шагам
			cur_prediction.setCoords(_angle, _distance);
			cur_prediction.add(
			get_path(v, w, _recentTargetHeading, i * time_discret + time_delta));
			return cur_prediction;
			}
		} // for под дискретам времени
	} // if( check_collisions )
	// Если оказались здесь, значит цель вообще не выходила за пределы поля
	cur_prediction.setCoords(_angle, _distance);
	cur_prediction.add(
	get_path(v, w, _recentTargetHeading, t));
	return cur_prediction;
}
/** Определить, выходит ли вектор, направленный из
* текущего положения танка за границу поля
*/
private boolean vector_out_of_field(GeomVektor vector) {
	double x, y;
	x = _AngryTankJr.getX() + vector.getX();
	y = _AngryTankJr.getY() + vector.getY();
	return (x < 0 + _AngryTankJr._collisionDelta
	|| x > _AngryTankJr._battleFieldWidth - _AngryTankJr._collisionDelta
	|| y < 0 + _AngryTankJr._collisionDelta
	|| y > _AngryTankJr._battleFieldHeight - _AngryTankJr._collisionDelta);
}
/** Определить по заданным параметрам элементарный путь, пройденный танком,
* предполагая скорости фиксированными
*/
public static GeomVektor get_path(double v, double w, double heading, double T) {
	GeomVektor path;
	if (Math.abs(w) > AngryTankJr.PRECISION) {
		// Движение по дуге
		double R = Math.abs(v / w);
		double to_circle_center, from_circle_center;
		to_circle_center = AngryTankJr.normalizeAngle(
		w * v >= 0 ?
		heading + AngryTankJr.HALF_PI :
		heading - AngryTankJr.HALF_PI);
		from_circle_center = AngryTankJr.normalizeAngle(to_circle_center + Math.PI + w * T);
		path = new GeomVektor(to_circle_center, R);
		path.addRadialVector(from_circle_center, R);
	} else {
		// Движение по прямой
		path = new GeomVektor(heading, v * T);
	}
	return path;
}
/** Вернуть имя цели */
public String getName() {
	return _name;
}	
	
}
