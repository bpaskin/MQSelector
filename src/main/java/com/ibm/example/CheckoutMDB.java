package com.ibm.example;

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;

/**
 * Consumes messages from the queue defined on the activation spec in server.xml.
 * The destination and the message selector ({@code host = 'checkout'}) are configured
 * there, so messages that do not match the selector stay on the queue.
 */
@MessageDriven(
        name = "CheckoutMDB",
        activationConfig = {
            @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue")
        })
public class CheckoutMDB implements MessageListener {

    private static final Logger LOG = Logger.getLogger(CheckoutMDB.class.getName());

    @Override
    public void onMessage(Message message) {
        try {
            String body = message instanceof TextMessage tm ? tm.getText() : message.toString();
            LOG.info("CheckoutMDB received [" + body + "] host=" + message.getStringProperty("host"));
        } catch (JMSException e) {
            LOG.log(Level.SEVERE, "Failed to read message", e);
            throw new jakarta.ejb.EJBException(e);
        }
    }
}
