package angrygeneration;

import robocode.*;
import java.util.Enumeration;
import java.util.Hashtable;
/**
* Класс "Cписок целей"
*/
public class TargetList {
	/** Ближайшая цель */
	public Target _closestTarget;
	/**
	* Изменение коэффициента отталкивания" от целей в
	* зависимости от количества целей
	*/
	private final double _targetsKDelta = 13;
	/** Коэффициент "отталкивания" от целей*/
	private double targets_k;
	/** Таблица целей*/
	private Hashtable targetsTable = new Hashtable(1);
	/** Время последнего сканирования */
	private long _lastScanCompletionTime = 0;
	/** Объект-супервизор */
	private AngryTankJr _AngryTankJr;
	public TargetList(AngryTankJr aCynical) {
		_AngryTankJr = aCynical;
	}
	/** Начало раунда */
	public void beginRound() {
		Enumeration targets_list = targetsTable.elements();
		Target t;
		// Коэффициент "отталкивания" от целей зависит от количества целей
		targets_k = 270 - _AngryTankJr.getOthers() * _targetsKDelta;
		if (targets_k <= 0) targets_k = 10;
			_closestTarget = null;
		while (targets_list.hasMoreElements()) {
			t = (Target) targets_list.nextElement();
			t.beginRound();
		}
		_lastScanCompletionTime = 0;
	}
	/** Начало шага */
	public void beginTurn() {
		Enumeration targets_list = targetsTable.elements();
		Target t;
		while (targets_list.hasMoreElements()) {
			t = (Target) targets_list.nextElement();
			t.targetRefresh();
		}
		_closestTarget = getClosestTarget(100);
	}
	/** Обновление цели */
	public void update(ScannedRobotEvent e) {
		Target t;
		String robot_name = e.getName();
		t = (Target) targetsTable.get(robot_name);
		if (t != null) {
			// Цель существует. Обновить информацию
			t.targetUpdate(e);
		} else {
			// Создать запись о новой обнаруженной цели
			t = new Target(_AngryTankJr, e);
			targetsTable.put(robot_name, t);
		}
	}
	/** Уничтожение цели */
	public void targetDestroyed(RobotDeathEvent e) {
		Target t;
		String robot_name = e.getName();
		t = (Target) targetsTable.get(robot_name);
		targets_k += _targetsKDelta;
		if (t != null)
			t.targetDestroyed();
	}
	/** Попадание в цель */
	public void hit(BulletHitEvent e) {
		Target t;
		String robot_name = e.getName();
		t = (Target) targetsTable.get(robot_name);
		if (t != null)
			t.targetHit();
	}
	/** Столкновение с целью */
	public void collision(HitRobotEvent e) {
		Target t;
		String robot_name = e.getName();
		t = (Target) targetsTable.get(robot_name);
		if (t != null)
			t.collision();
	}
	/** Попадание в нас */
	public void hitByBullet(HitByBulletEvent e) {
		Target t;
		String robot_name = e.getName();
		t = (Target) targetsTable.get(robot_name);
		if (t != null)
			t.hitByBullet();
	}
	/** Вернуть ближайшую цель */
	public Target getClosestTarget(double time_on_aiming) {
		Enumeration targets_list = targetsTable.elements();
		Target chosen_target = null, t = null;
		// Среди целей, на которые орудие успеет навестись быстрее,
		// чем за заданное время, выбрать ближайшую
		while (targets_list.hasMoreElements()) {
			t = (Target) targets_list.nextElement();
			if (t.isTracked()) {
				if (Math.abs(AngryTankJr.getAngleDiff(_AngryTankJr.getGunner().getCurHeading(), t._angle))
				< time_on_aiming * AngryTankJr.MAX_GUN_ROTATION_SPEED) {
					// Цель в зоне досягаемости орудия
					if (chosen_target == null)
						chosen_target = t;
					else if (t._distance < chosen_target._distance)
						chosen_target = t;
				}
			}
		}
		return chosen_target;
	}
	/** Вернуть направление на удаление от видимых целей */
	public GeomVektor getReactionVector() {
		Enumeration targets_list = targetsTable.elements();
		Target t;
		GeomVektor reaction = new GeomVektor();
		while (targets_list.hasMoreElements()) {
			t = (Target) targets_list.nextElement();
			if (t.isTracked()) {
				reaction.addRadialVector(AngryTankJr.normalizeAngle(t._angle + Math.PI), targets_k / t._distance);
			}
		}
		return reaction;
	}
	/** Проверить, что очередной цикл сканирования завершен */
	public boolean scanCompleted() {
		Enumeration targets_list = targetsTable.elements();
		Target t;
		int tracked_targets = 0;
		if (!targets_list.hasMoreElements()) return false;
		if (_AngryTankJr._currentTime < 20) return false;
		// Проверить, все ли сопровождаемые цели отсканированы
		while (targets_list.hasMoreElements()) {
			t = (Target) targets_list.nextElement();
			if (t.isTracked()) {
				// Цель сопровождается
				if (!t.wasUpdatedAfter(_lastScanCompletionTime)) {
					// Цель не была сканирована в новом проходе,
					// следовательно, новый проход сканирования еще не завершен
					return false;
				}
				tracked_targets++;
			}
		}
		// Здесь оказались, если:
		// - список целей не пуст;
		// - не начало раунда;
		// - все сопровождаемые цели (если они есть) отсканированы
		// Следовательно, если есть сопровождаемые цели, то цикл сканирования завершен
		if (tracked_targets > 0) {
			_lastScanCompletionTime = _AngryTankJr._currentTime;
			return true;
		} else
			return false;
	}
	/** Отобразить статистику*/
	public void showStatistics() {
		Enumeration targets_list = targetsTable.elements();
		Target t;
		while (targets_list.hasMoreElements()) {
			t = (Target) targets_list.nextElement();
			t.logStatisitics();
		}
	}
}