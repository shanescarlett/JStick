package net.scarlettsystems.java.jstick;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.*;

/**
 * A Java Swing component that mimics a joystick on the screen. The mouse can be used to drag the thumb stick on the
 * pad and the joystick will output either a cartesian or polar value of the stick's movement.
 */
public class JStick extends javax.swing.JPanel
{
	private static final float DEFAULT_STICK_SIZE = 0.33f;
	private static final float DEFAULT_ARROW_SIZE = 0.06f;
	private static final float DEFAULT_DEAD_ZONE = 0;

	private int padDiameter;
	private int padRadius;
	private int stickRadius;
	private float stickSize;
	private float arrowSize;
	private float deadZone;
	private int zeroPointX;
	private int zeroPointY;

	private int outlineWidth = 1;

	private Color outlineColour = Color.BLACK;
	private Color padColour = getBackground().brighter();
	private Color stickColour = getBackground().brighter().brighter();
	private Color arrowColour = getBackground();

	private boolean drawOutline = false;
	private boolean invertY = false;

	private final Point stickPosition = new Point();
	private final Point clickBeginPoint = new Point(-1, -1);
	private final ArrayList<JoystickListener> mJoystickListeners;

	private boolean dragFlag = false;

	/**
	 * The listener interface for receiving joystick events.
	 */
	public interface JoystickListener
	{
		void onJoystickMoved(JStick j);
		void onJoystickClicked(JStick j);
	}

	public JStick()
	{
		mJoystickListeners = new ArrayList<>();
		setStickSize(DEFAULT_STICK_SIZE);
		setArrowSize(DEFAULT_ARROW_SIZE);
		setDeadZone(DEFAULT_DEAD_ZONE);

		MouseAdapter mouseAdapter = new MouseAdapter()
		{

			private void repaintAndTriggerListeners()
			{
				SwingUtilities.getRoot(JStick.this).repaint();
				for(JoystickListener l : mJoystickListeners)
				{
					l.onJoystickMoved(JStick.this);
				}
			}

			@Override
			public void mousePressed(final MouseEvent e)
			{
				if(SwingUtilities.isLeftMouseButton(e) && isPointOnStick(e.getX(), e.getY()))
				{
					clickBeginPoint.x = e.getX();
					clickBeginPoint.y = e.getY();
					dragFlag = false;
				}
			}

			@Override
			public void mouseDragged(MouseEvent e)
			{
				if (SwingUtilities.isLeftMouseButton(e) && clickBeginPoint.x >= 0 && clickBeginPoint.y >= 0)
				{
					updateThumbPos(e.getX() - clickBeginPoint.x, e.getY() - clickBeginPoint.y);
					repaintAndTriggerListeners();
					dragFlag = true;
				}
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (SwingUtilities.isLeftMouseButton(e))
				{
					if(dragFlag)
					{
						centerThumbPad();
						clickBeginPoint.x = -1;
						clickBeginPoint.y = -1;
						repaintAndTriggerListeners();
					}
					else
					{
						for(JoystickListener l : mJoystickListeners)
						{
							l.onJoystickClicked(JStick.this);
						}
					}
				}
			}
		};
		addMouseMotionListener(mouseAdapter);
		addMouseListener(mouseAdapter);
		setOpaque(false);

		centerThumbPad();
	}

	private void centerThumbPad()
	{
		stickPosition.x = 0;
		stickPosition.y = 0;
	}

	/**
	 * Checks whether the specified point is within the boundaries of the thumb stick
	 * @param x x position
	 * @param y y position
	 * @return whether the point is within the stick
	 */
	private boolean isPointOnStick(int x, int y)
	{
		return Math.sqrt(Math.pow((float)getWidth() / 2 - x - stickPosition.x, 2) + Math.pow((float)getHeight() / 2 - y - stickPosition.y, 2)) <= stickRadius;
	}


	/**
	 * update both thumbPos
	 *
	 * @param mouseX the x position of cursor that has clicked in the joystick panel
	 * @param mouseY the y position of cursor that has clicked in the joystick panel
	 */
	private void updateThumbPos(int mouseX, int mouseY)
	{
		//thumbPos.x = Math.min(Math.max(mouseX, zeroPointX), zeroPointX + padDiameter) - getWidth() / 2;
		//thumbPos.y = -(Math.min(Math.max(mouseY, zeroPointY), zeroPointY + padDiameter) - getHeight() / 2);
		stickPosition.x = mouseX;
		stickPosition.y = -mouseY;
		//Divide the values to keep within circle
		double magnitude = Math.sqrt(Math.pow(stickPosition.x, 2) + Math.pow(stickPosition.y, 2));
		double divider = Math.max(magnitude / padRadius, 1);
		stickPosition.x = (int)Math.round(stickPosition.x / divider);
		stickPosition.y = (int)Math.round(stickPosition.y / divider);
	}

	/**
	 * Gets the X-position of the thumb stick where a value of 1 is fully-right, -1 is fully-left, and 0 is centered.
	 * @return x position of the stick
	 */
	public float getStickX()
	{
		if(getStickMagnitude() < deadZone)
			return 0;
		else
			return (float) stickPosition.x / padRadius;
	}

	/**
	 * Gets the Y-position of the thumb stick where a value of 1 is fully-up, -1 is fully-up, and 0 is centered.
	 * @return y position of the stick
	 */
	public float getStickY()
	{
		if(getStickMagnitude() < deadZone)
			return 0;
		else
			return invertY ? -(float) stickPosition.y / padRadius : (float) stickPosition.y / padRadius;
	}

	/**
	 * Gets the Euclidean magnitude of the thumb stick from the centre point.
	 * @return magnitude of the stick position
	 */
	public float getStickMagnitude()
	{
		return (float)Math.sqrt(Math.pow(stickPosition.x, 2) + Math.pow(stickPosition.y, 2));
	}

	/**
	 * Gets the angle of the thumb stick in radians where positive angles are counter-clockwise from the positive
	 * x-axis, and negative angles are clockwise from the positive x-axis.
	 * @return angle in radians
	 */
	public float getStickAngleRadians()
	{
		return (float)Math.atan2(stickPosition.y, stickPosition.x);
	}

	/**
	 * Gets the angle of the thumb stick in degrees where positive angles are counter-clockwise from the positive
	 * x-axis, and negative angles are clockwise from the positive x-axis.
	 * @return angle in degrees
	 */
	public float getStickAngleDegrees()
	{
		return getStickAngleRadians() * 180 / (float)Math.PI;
	}

	@Override
	protected void paintComponent(final Graphics g)
	{
		super.paintComponent(g);
		//Calculate values
		int workingArea = Math.min(getWidth(), getHeight());
		padDiameter = Math.round(workingArea / (1 + stickSize));
		padRadius = padDiameter / 2;
		int stickDiameter = Math.round(workingArea * stickSize / (1 + stickSize));
		stickRadius = stickDiameter / 2;
		int arrowRadius = Math.round((float) padDiameter * arrowSize);
		if(getWidth() < getHeight())
		{
			zeroPointX = stickRadius;
			zeroPointY = getHeight() / 2 - padRadius;
		}
		else
		{
			zeroPointX = getWidth() / 2 - padRadius;
			zeroPointY = stickRadius;
		}

		//Draw the background pad
		if(drawOutline)
		{
			g.setColor(outlineColour);
			g.fillOval(zeroPointX, zeroPointY, padDiameter, padDiameter);
			g.setColor(padColour);
			g.fillOval(zeroPointX + outlineWidth,
					zeroPointY + outlineWidth,
					padDiameter - (outlineWidth * 2),
					padDiameter - (outlineWidth * 2));
		}
		else
		{
			g.setColor(padColour);
			g.fillOval(zeroPointX, zeroPointY, padDiameter, padDiameter);
		}

		//Draw background arrows
		g.setColor(arrowColour);
		int arrowInset = (stickRadius + padRadius) / 2;
		int xCentrePoint = getWidth() / 2;
		int yCentrePoint = getHeight() / 2;
		fillEquilateralTriangle(g, xCentrePoint, yCentrePoint - arrowInset, arrowRadius, -(float)Math.PI / 2);
		fillEquilateralTriangle(g, xCentrePoint, yCentrePoint + arrowInset, arrowRadius, (float)Math.PI / 2);
		fillEquilateralTriangle(g, xCentrePoint - arrowInset, yCentrePoint, arrowRadius, (float)Math.PI);
		fillEquilateralTriangle(g, xCentrePoint + arrowInset, yCentrePoint, arrowRadius, 0);

		int thumbStartX = xCentrePoint - stickRadius + stickPosition.x;
		int thumbStartY = yCentrePoint - stickRadius - stickPosition.y;
		if(drawOutline)
		{
			g.setColor(outlineColour);
			g.fillOval(thumbStartX, thumbStartY, stickDiameter, stickDiameter);
			g.setColor(stickColour);
			g.fillOval(thumbStartX + outlineWidth,
					thumbStartY + outlineWidth,
					stickDiameter - (outlineWidth * 2),
					stickDiameter - (outlineWidth * 2));
		}
		else
		{
			g.setColor(stickColour);
			g.fillOval(thumbStartX, thumbStartY, stickDiameter, stickDiameter);
		}
	}

	private void fillEquilateralTriangle(Graphics g, int x, int y, int a, float theta)
	{
		int centroid = (int)Math.round(a * Math.sqrt(3) / 6);
		int[] xa = {x + (int)Math.round(a * Math.sqrt(3) / 3),	x - centroid, 	x - centroid};
		int[] ya = {y,					y - a / 2,		y + a / 2};
		rotatePoints(xa, ya, x, y, theta);
		g.fillPolygon(xa, ya, 3);
	}

	private void rotatePoints(int[] x, int[] y, int x0, int y0, float theta)
	{
		if(x.length != y.length)
		{
			throw new IllegalArgumentException("Array sizes must be equal");
		}
		Point origin = new Point(x0, y0);
		for(int i = 0; i < x.length; i++)
		{
			Point p = new Point(x[i], y[i]);
			Point n = rotatePoint(p, origin,theta);
			x[i] = n.x;
			y[i] = n.y;
		}
	}

	private Point rotatePoint(Point p, Point o, float theta)
	{
		Point n = new Point();
		n.x = (int)Math.round(Math.cos(theta) * (p.x-o.x) - Math.sin(theta) * (p.y-o.y) + o.x);
		n.y =  (int)Math.round(Math.sin(theta) * (p.x-o.x) + Math.cos(theta) * (p.y-o.y) + o.y);
		return n;
	}

	//Getters and setters

	/**
	 * Gets the colour of the outline.
	 * @return colour
	 */
	public Color getOutlineColour()
	{
		return outlineColour;
	}

	/**
	 * Sets the colour of the outline. This method has no effect if setDrawOutline is set to false.
	 * @param outlineColour colour
	 */
	public void setOutlineColour(Color outlineColour)
	{
		this.outlineColour = outlineColour;
	}

	/**
	 * Gets the colour of the circular pad of the joystick.
	 * @return colour
	 */
	public Color getPadColour()
	{
		return padColour;
	}

	/**
	 * Sets the colour of the circular pad of the joystick.
	 * @param padColour colour
	 */
	public void setPadColour(Color padColour)
	{
		this.padColour = padColour;
	}

	/**
	 * Gets the colour of the joystick thumb.
	 * @return colour
	 */
	public Color getStickColour()
	{
		return stickColour;
	}

	/**
	 * Sets the colour of the joystick thumb.
	 * @param stickColour colour
	 */
	public void setStickColour(Color stickColour)
	{
		this.stickColour = stickColour;
	}

	/**
	 * Gets the colour of the arrows displayed around the stick.
	 * @return colour
	 */
	public Color getArrowColour()
	{
		return arrowColour;
	}

	/**
	 * Sets the colour of the arrows displayed around the stick.
	 * @param arrowColour colour
	 */
	public void setArrowColour(Color arrowColour)
	{
		this.arrowColour = arrowColour;
	}

	/**
	 * Gets whether an outline is set to be drawn on the component.
	 * @return is draw outline enabled
	 */
	public boolean isDrawOutline()
	{
		return drawOutline;
	}

	/**
	 * Sets whether an outline is to be drawn around the component.
	 * @param drawOutline set draw outline
	 */
	public void setDrawOutline(boolean drawOutline)
	{
		this.drawOutline = drawOutline;
	}

	/**
	 * Gets the size of the thumb stick of the joystick, as a proportion of the pad's overall diameter.
	 * @return size the size of the stick as a portion
	 */
	public float getStickSize()
	{
		return stickSize;
	}

	/**
	 * Sets the size of the thumb stick of the joystick, as a proportion of the pad's overall diameter.
	 * Default value = 0.33, range from 0 to 1.
	 * @param size the size of the stick as a portion
	 */
	public void setStickSize(float size)
	{
		if(size < 0){throw new IllegalArgumentException("Size must be greater than 0");}
		if(size > 1){throw new IllegalArgumentException("Size must be smaller than 1");}
		stickSize = size;
	}

	/**
	 * Gets the size of the thumb stick of the joystick, as a proportion of the pad's overall diameter.
	 * @return size the size of the arrows as a portion
	 */
	public float getArrowSize()
	{
		return arrowSize;
	}

	/**
	 * Sets the size of the arrows around the stick, as a proportion of the pad's overall diameter.
	 * Default value = 0.06, range from 0 to 1.
	 * @param size the size of the arrows as a portion
	 */
	public void setArrowSize(float size)
	{
		if(size < 0){throw new IllegalArgumentException("Size must be greater than 0");}
		if(size > 1){throw new IllegalArgumentException("Size must be smaller than 1");}
		arrowSize = size;
	}

	/**
	 * Gets the dead zone, or the proportional region at the centre point where the joystick does not output a value.
	 * @return value proportional size of the dead zone
	 */
	public float getDeadZone()
	{
		return deadZone;
	}

	/**
	 * Sets the dead zone, or the proportional region at the centre point where the joystick does not output a value.
	 * @param value proportional size of the dead zone
	 */
	public void setDeadZone(float value)
	{
		if(value < 0){throw new IllegalArgumentException("Value must be greater than 0");}
		if(value > 1){throw new IllegalArgumentException("Value must be smaller than 1");}
		deadZone = value;
	}

	public void addJoystickListener(JoystickListener l)
	{
		mJoystickListeners.add(l);
	}

	public void removeJoystickListener(JoystickListener l)
	{
		mJoystickListeners.remove(l);
	}

	public void clearJoystickListeners()
	{
		mJoystickListeners.clear();
	}

	/**
	 * Sets whether the output of the Y-axis is inverted, as to mimic flight controls, etc. Only affects the value
	 * returned by getStickY().
	 * @param invert inversion state
	 */
	public void setInvertYAxis(boolean invert)
	{
		invertY = invert;
	}

	/**
	 * Gets whether the output of the Y-axis is inverted, as to mimic flight controls, etc. Only affects the value
	 * returned by getStickY().
	 * @return inversion state
	 */
	public boolean isYAxisInverted()
	{
		return invertY;
	}
}
