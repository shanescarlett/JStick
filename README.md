# JStick

JStick, the simple joystick for Java Swing.<br><br>
<img src="https://raw.githubusercontent.com/shanescarlett/JStick/master/jstick_demo.gif" />

## Getting Started
### Prerequisites
JStick is compatible with Java language level 8 or newer.
### Installation
Add JStick to your Maven project by adding the following dependency to ```pom.xml```:
```xml
<dependency>
    <groupId>net.scarlettsystems.java</groupId>
    <artifactId>jstick</artifactId>
    <version>0.0.1</version>
</dependency>
```

## Usage
The component may be instantiated by simply calling its constructor:
```java
JStick myJoystick = new JStick();
```
Adding the component to a panel will automatically have the joystick sized to fill its bounds:
```java
JPanel panel = new JPanel();
panel.add(myJoystick);
```
Attach a listener to be fired each time motion is detected on the joystick:
```java
myJoystick.addJoystickListener(new JStick.JoystickListener()
		{
			@Override
			public void onJoystickMoved(JStick j)
			{
				int x = j.getStickX();
				int y = j.getStickY();
				//Do something useful
			}

			@Override
			public void onJoystickClicked(JStick j)
			{

			}
		});
```
There are several other configurable parameters including the colour and dead-zone. See the docs for more information.

## Authors

* **Shane Scarlett** - *core development* - [Scarlett Systems](https://scarlettsystems.net)

## Acknowledgements
This component was inspired by Sam Arthur Gilliam's Stackoverflow answer [here](https://stackoverflow.com/questions/16439621/java-on-screen-virtual-joystick-control), and contains heavily modified code from said post.


## License

This project is licensed under the Apache 2.0 License.