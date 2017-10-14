package cloud;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

class InterfaceBot {

    void openTWS() throws AWTException {
        Robot r = new Robot();
        r.setAutoDelay(50);
        r.setAutoWaitForIdle(true);
        r.delay(500);
        r.mouseMove(Parameters.iconC1,Parameters.iconC2);
        r.delay(500);
        OrderPlacer.robotDelay+=1050;
        leftClick();
        leftClick();
    }
    void loginTWS() throws AWTException{
        Robot r = new Robot();
        r.setAutoDelay(50);
        r.setAutoWaitForIdle(true);
        r.delay(500);
        type(Parameters.username);
        r.delay(50);
        type(KeyEvent.VK_TAB);
        r.delay(500);
        type(Parameters.password);
        r.delay(500);
        type(KeyEvent.VK_ENTER);
        r.delay(500);
        OrderPlacer.robotDelay+=2050;
    }
    void uAndE() throws AWTException{
        Robot r = new Robot();
        r.setAutoDelay(50);
        r.setAutoWaitForIdle(true);
        r.delay(500);
        r.mouseMove(Parameters.uAndEC1,Parameters.uAndEC2);
        r.delay(500);
        OrderPlacer.robotDelay+=1050;
        leftClick();
    }
    private void leftClick() throws AWTException{
        Robot r = new Robot();
        r.setAutoDelay(50);
        r.setAutoWaitForIdle(true);
        r.mousePress(InputEvent.BUTTON1_MASK);
        r.delay(100);
        r.mouseRelease(InputEvent.BUTTON1_MASK);
        r.delay(100);
        OrderPlacer.robotDelay+=300;
    }
    private void type(int i) throws AWTException{
        Robot r = new Robot();
        r.setAutoDelay(50);
        r.setAutoWaitForIdle(true);
        r.delay(50);
        r.keyPress(i);
        r.keyRelease(i);
        OrderPlacer.robotDelay+=150;
    }
    private void type(String s) throws AWTException{
        Robot r = new Robot();
        r.setAutoDelay(50);
        r.setAutoWaitForIdle(true);
        byte[] bytes = s.getBytes();
        for(byte b:bytes)
        {
            int code = b;
            if(code>96&&code<123){code=code-32;}
            r.delay(50);
            r.keyPress(code);
            r.keyRelease(code);
            OrderPlacer.robotDelay+=150;
        }
    }
}
