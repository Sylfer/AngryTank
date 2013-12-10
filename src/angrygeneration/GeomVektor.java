package angrygeneration;

public class GeomVektor {

	/** Угол наклона вектора */
	private double _angle;
	/** Длина вектора */
	private double _radius;	public GeomVektor(int i, int j) {
		// TODO Auto-generated constructor stub
	}
	/** Конструктор, создающий вектор нулевой длины */
	
	GeomVektor() {
		this(0,0);
	}

	/** Конструктор, создающий вектор по заданным углу и длине 
	 * @return */
	GeomVektor(double aAngle, double aRadius) {
		setCoords(aAngle, aRadius);
	}
	/** Сбросить координаты */
	public void reset() {
		setCoords(0, 0);
	}
	/** Сбросить координаты */
	public void setCoords(double a1, double R1) {
		setAngle(a1);
		setRadius(R1);
	};
	/** Прибавить вектор */
	public void add(GeomVektor vect2) {
		addCatresianVector(vect2.getX(), vect2.getY());
	}
	/** Прибавить вектор, заданный в декартовой системе координат */
	public void addCatresianVector(double aX, double aY) {
		double newX = getX() + aX;
		double newY = getY() + aY;
		setAngle(AngryTankJr.normalizeAngle(AngryTankJr.getAngle(newX, newY)));
		setRadius(Math.sqrt(newX * newX + newY * newY));
	}
	/** Прибавить вектор, заданный в радиальной системе координат */
	public void addRadialVector(double aAngle, double aRadius) {
		double newX = getX() + aRadius * Math.sin(aAngle);
		double newY = getY() + aRadius * Math.cos(aAngle);
		setAngle(AngryTankJr.normalizeAngle(AngryTankJr.getAngle(newX, newY)));
		setRadius(Math.sqrt(newX * newX + newY * newY));
	}
	/** Получить угол наклона вектора */
	public double getAngle() {
		return _angle;
	}
	/** Получить радиус вектора */
	public double getRadius() {
		return _radius;
	}
	/** Получить x-координату вектора */
	public double getX() {
		return _radius * Math.sin(_angle);
	}
	/** Получить y-координату вектора */
	public double getY() {
		return _radius * Math.cos(_angle);
	}
	/** Изменить радиус вектора */
	private void setRadius(double aR) {
		_radius = aR;
	}
	/** Изменить угол наклона вектора */
	public void setAngle(double aAngle) {
		_angle = aAngle;
	}	
	
}
