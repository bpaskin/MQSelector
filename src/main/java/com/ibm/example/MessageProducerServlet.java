package com.ibm.example;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.Resource;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSProducer;
import jakarta.jms.Queue;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Writes 1000 messages to the JMS queue. A random subset of the messages is tagged
 * with the String property {@code host=checkout} so a consumer can select on it.
 */
@WebServlet(urlPatterns = "/send")
public class MessageProducerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(MessageProducerServlet.class.getName());

    static final String HOST_PROPERTY = "host";
    static final String HOST_CHECKOUT = "checkout";
    static final String HOST_OTHER = "other";
    private static final int MESSAGE_COUNT = 1000;

    @Resource(lookup = "jms/MQSelectorCF")
    private ConnectionFactory connectionFactory;

    @Resource(lookup = "jms/MQSelectorQueue")
    private Queue queue;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");

        int checkoutCount = 0;
        try (JMSContext context = connectionFactory.createContext()) {
            JMSProducer producer = context.createProducer();
            for (int i = 1; i <= MESSAGE_COUNT; i++) {
                // Randomly (roughly one in three) mark the message for the "checkout" host.
                boolean checkout = ThreadLocalRandom.current().nextInt(3) == 0;
                producer.setProperty(HOST_PROPERTY, checkout ? HOST_CHECKOUT : HOST_OTHER);
                producer.send(queue, "Message " + i);
                if (checkout) {
                    checkoutCount++;
                }
            }
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Failed to send messages", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("Unable to send messages");
            return;
        }

        LOG.info("Sent " + MESSAGE_COUNT + " messages, " + checkoutCount + " with host=" + HOST_CHECKOUT);
        resp.getWriter().println(
                "Sent " + MESSAGE_COUNT + " messages; " + checkoutCount + " tagged host=" + HOST_CHECKOUT);
    }
}
