package angrygeneration;

import java.awt.geom.Point2D;
import java.util.Enumeration;
import java.util.Hashtable;

import robocode.util.Utils;
import java.awt.geom.*;
import static java.lang.Math.signum;
import static java.lang.Math.random;

public class Movement {
	
	/** Коэффициент "отталкивания" от стен */
	private final double WALLS_COEFF = 100;
	
	/** Направление движения*/
	private double _oldHeading, _curHeading;	

	private double _oldX, _oldY, _curX, _curY;	

	/** Направление движения */
	private GeomVektor _direction = new GeomVektor();
	
	/** На сколько надо повернуть на данном шаге*/
	private double _da;
	/** С какой скоростью надо двигаться на данном шаге */
	private double _speed;
	
	double midpointstrength = 0;	//The strength of the gravity point in the middle of the field
	int midpointcount = 0;	
	
	Hashtable targets;
	
	private AngryTankJr _robot;
	
	public Movement(AngryTankJr aRobot) {
		_robot = aRobot;
		_direction.reset();	
		_oldX = _curX = _robot.getX();
		_oldY = _curY = _robot.getY();
		
		_oldHeading = _robot.getHeadingRadians();
		_curHeading = _robot.getHeadingRadians();
		_da = 0;
		_speed = AngryTankJr.MAX_SPEED;		
	}	
	
	/** Проверка на близость врага */
	public boolean enemyIsNear() {
		boolean result = true;
		
		if (_robot.getTargets()._closestTarget != null) {
			result = _robot.getTargets()._closestTarget._distance < 300;
		}
		
		return result;
	}
	/** Проверка на близость к стене */
	public boolean wallIsNear() {
		double collision_delta = _robot._robotSizeHalved + 40;
		
		boolean result =
		_curX < 0 + collision_delta
		|| _curX > _robot._battleFieldWidth - collision_delta
		|| _curY < 0 + collision_delta
		|| _curY > _robot._battleFieldHeight - collision_delta;

		return result;
	}	

	private double getDistance() {
        // вычисление дистанции движения элементарно
        return 200 - 400 * random();
    }

    private double getBodyTurn(double enemyX, double enemyY) {
        // А вот вычисление угла поворота посложее
        final double alphaToMe = angleTo(enemyX, enemyY, _robot.getX(), _robot.getY());

        // определяем угловое направление относительно противника (по часовой стрелке, либо против) ...
        final double lateralDirection = signum((_robot.getVelocity() != 0 ? _robot.getVelocity() : 1) * Math.sin(Utils.normalRelativeAngle(_robot.getHeadingRadians() - alphaToMe)));
        // получаем желаемое направление движения
        final double desiredHeading = Utils.normalAbsoluteAngle(alphaToMe + Math.PI / 2 * lateralDirection);
        // нормализуем направление по скорости
        final double normalHeading = _robot.getVelocity() >= 0 ? _robot.getHeadingRadians() : Utils.normalAbsoluteAngle(_robot.getHeadingRadians() + Math.PI);
        // и возвращаем угол поворта
        return Utils.normalRelativeAngle(desiredHeading - normalHeading);
    }
    
    private static double angleTo(double baseX, double baseY, double x, double y) {
        double theta = Math.asin((y - baseY) / Point2D.distance(x, y, baseX, baseY)) - Math.PI / 2;
        if (x >= baseX && theta < 0) {
            theta = -theta;
        }
        return (theta %= Math.PI * 2) >= 0 ? theta : (theta + Math.PI * 2);
    }
    
	private void getDirection() {
		_direction.reset();
		_direction.addCatresianVector(WALLS_COEFF / _curX, 0);
		_direction.addCatresianVector(-WALLS_COEFF / (_robot._battleFieldWidth - _curX), 0);
		_direction.addCatresianVector(0, WALLS_COEFF / _curY);
		_direction.addCatresianVector(0, -WALLS_COEFF / (_robot._battleFieldHeight - _curY));
		_direction.add(_robot.getTargets().getReactionVector());
	}
	
	private void calculate_movement_order(boolean aReverseAllowed) {
		double da1, // Угол, на который надо повернуться при движении вперед
		da2; // Угол, на который надо повернуться при движении назад
		da1 = _robot.getAngleDiff(_curHeading, _direction.getAngle());
		da2 = _robot.getAngleDiff(_curHeading, _robot.normalizeAngle(_direction.getAngle() - Math.PI));
		if (aReverseAllowed) {
			if (Math.abs(da1) < Math.abs(da2)) {
				_da = da1;
				_speed = AngryTankJr.MAX_SPEED;
			} else {
				_da = da2;
				_speed = -AngryTankJr.MAX_SPEED;
			}
		} else {
			if (_speed > 0) {
				_da = da1;
			} else {
				_da = da2;
			}
		}
	}
	
	// Antigrav
	
	void antiGravMove() {
   		double xforce = 0;
	    double yforce = 0;
	    double force;
	    double ang;
	    GravPoint p;
		Enemy en;
    	Enumeration e = targets.elements();
	    //cycle through all the enemies.  If they are alive, they are repulsive.  Calculate the force on us
		while (e.hasMoreElements()) {
    	    en = (Enemy)e.nextElement();
			if (en.live) {
				p = new GravPoint(en.x,en.y, -1000);
		        force = p.power/Math.pow(getRange(_robot.getX(),_robot.getY(),p.x,p.y),2);
		        //Find the bearing from the point to us
		        ang = normaliseBearing(Math.PI/2 - Math.atan2(_robot.getY() - p.y, _robot.getX() - p.x)); 
		        //Add the components of this force to the total force in their respective directions
		        xforce += Math.sin(ang) * force;
		        yforce += Math.cos(ang) * force;
			}
	    }
	    
		/**The next section adds a middle point with a random (positive or negative) strength.
		The strength changes every 5 turns, and goes between -1000 and 1000.  This gives a better
		overall movement.**/
		midpointcount++;
		if (midpointcount > 5) {
			midpointcount = 0;
			midpointstrength = (Math.random() * 2000) - 1000;
		}
		p = new GravPoint(_robot.getBattleFieldWidth()/2, _robot.getBattleFieldHeight()/2, midpointstrength);
		force = p.power/Math.pow(getRange(_robot.getX(),_robot.getY(),p.x,p.y),1.5);
	    ang = normaliseBearing(Math.PI/2 - Math.atan2(_robot.getY() - p.y, _robot.getX() - p.x)); 
	    xforce += Math.sin(ang) * force;
	    yforce += Math.cos(ang) * force;
	   
	    /**The following four lines add wall avoidance.  They will only affect us if the bot is close 
	    to the walls due to the force from the walls decreasing at a power 3.**/
	    xforce += 5000/Math.pow(getRange(_robot.getX(), _robot.getY(), _robot.getBattleFieldWidth(), _robot.getY()), 3);
	    xforce -= 5000/Math.pow(getRange(_robot.getX(), _robot.getY(), 0, _robot.getY()), 3);
	    yforce += 5000/Math.pow(getRange(_robot.getX(), _robot.getY(), _robot.getX(), _robot.getBattleFieldHeight()), 3);
	    yforce -= 5000/Math.pow(getRange(_robot.getX(), _robot.getY(), _robot.getX(), 0), 3);
	    
	    //Move in the direction of our resolved force.
	    goTo(_robot.getX()-xforce,_robot.getY()-yforce);
	}
	
	/**Move towards an x and y coordinate**/
	void goTo(double x, double y) {
	    double dist = 20; 
	    double angle = Math.toDegrees(absbearing(_robot.getX(),_robot.getY(),x,y));
	    double r = turnTo(angle);
	    _robot.setAhead(dist * r);
	}


	/**Turns the shortest angle possible to come to a heading, then returns the direction the
	the bot needs to move in.**/
	int turnTo(double angle) {
	    double ang;
    	int dir;
	    ang = normaliseBearing(_robot.getHeading() - angle);
	    if (ang > 90) {
	        ang -= 180;
	        dir = -1;
	    }
	    else if (ang < -90) {
	        ang += 180;
	        dir = -1;
	    }
	    else {
	        dir = 1;
	    }
	    _robot.setTurnLeft(ang);
	    return dir;
	}
	
	public double getRange( double x1,double y1, double x2,double y2 )
	{
		double xo = x2-x1;
		double yo = y2-y1;
		double h = Math.sqrt( xo*xo + yo*yo );
		return h;	
	}	
	
	double normaliseBearing(double ang) {
		if (ang > Math.PI)
			ang -= 2*Math.PI;
		if (ang < -Math.PI)
			ang += 2*Math.PI;
		return ang;
	}
	
	public double absbearing( double x1,double y1, double x2,double y2 )
	{
		double xo = x2-x1;
		double yo = y2-y1;
		double h = getRange( x1,y1, x2,y2 );
		if( xo > 0 && yo > 0 )
		{
			return Math.asin( xo / h );
		}
		if( xo > 0 && yo < 0 )
		{
			return Math.PI - Math.asin( xo / h );
		}
		if( xo < 0 && yo < 0 )
		{
			return Math.PI + Math.asin( -xo / h );
		}
		if( xo < 0 && yo > 0 )
		{
			return 2.0*Math.PI - Math.asin( -xo / h );
		}
		return 0;
	}	
	
    public void move(double enemyX, double enemyY) {
    
    	//antiGravMove();
		_oldHeading = _curHeading;
		_curHeading = _robot.getHeadingRadians();
		_oldX = _curX;
		_curX = _robot.getX();
		_oldY = _curY;
		_curY = _robot.getY();
		_da = 0;
    	
    	if (wallIsNear() || enemyIsNear()) {
    		_robot.setMaxVelocity(10);
    		getDirection();
    		
    		_direction.setCoords(_direction.getAngle(), 1);
    		double angle_diff = _robot.normalizeAngle(_direction.getAngle() +
    		(_robot._randomizer.nextBoolean() ? AngryTankJr.HALF_PI : -AngryTankJr.HALF_PI));
    		_direction.addRadialVector(angle_diff, 0.4);
    		
    		calculate_movement_order(true);
    		
    		_robot.setTurnRightRadians(_da);

    		_robot.setAhead(_speed * 100);	
    	} else {
    		
		    final double bodyTurn = getBodyTurn(enemyX, enemyY);
	    	_robot.setTurnRightRadians(bodyTurn);
		    
		    if (_robot.getDistanceRemaining() == 0) {
			    final double distance = getDistance();		    		    
			    _robot.setAhead(distance);
		    }
    	}
    }
    
    class Enemy {
    	/*
    	 * ok, we should really be using accessors and mutators here,
    	 * (i.e getName() and setName()) but life's too short.
    	 */
    	String name;
    	public double bearing,heading,speed,x,y,distance,changehead;
    	public long ctime; 		//game time that the scan was produced
    	public boolean live; 	//is the enemy alive?
    	public Point2D.Double guessPosition(long when) {
    		double diff = when - ctime;
    		double newY = y + Math.cos(heading) * speed * diff;
    		double newX = x + Math.sin(heading) * speed * diff;
    		
    		return new Point2D.Double(newX, newY);
    	}
    }

    /**Holds the x, y, and strength info of a gravity point**/
    class GravPoint {
        public double x,y,power;
        public GravPoint(double pX,double pY,double pPower) {
            x = pX;
            y = pY;
            power = pPower;
        }
    }    
	
}
