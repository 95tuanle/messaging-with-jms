package humber.n01414195;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.Scanner;

@SpringBootApplication
@EnableJms
public class Application {

    @Bean
    public JmsListenerContainerFactory<?> myFactory(ConnectionFactory connectionFactory,
                                                    DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        // This provides all boot's default to this factory, including the message converter
        configurer.configure(factory, connectionFactory);
        // You could still override some of Boot's default if necessary.
        return factory;
    }

    @Bean // Serialize message content to json using TextMessage
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

    public static void main(String[] args) throws Exception {

        BrokerService broker = new BrokerService();
        broker.setBrokerName("localhost");
        broker.setUseJmx(true);
        broker.addConnector("tcp://localhost:61616");
        broker.start();

        // Launch the application
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

        JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);

        // Send a message with a POJO - the template reuse the message converter
        System.out.println("Sending an email message.");
        Email email = new Email("tlna95@icloud.com", "Nguyen Anh Tuan Le - N01414195");
        jmsTemplate.convertAndSend("mailbox", email);
        Scanner myObj = new Scanner(System.in);
        while (true) {
            System.out.print("Please enter anything to resend the email message or enter 'exit' to exit the program:  ");
            String userName = myObj.nextLine();  // Read user input
            if (userName.equals("exit")) {
                break;
            }
            System.out.println("Sending an email message.");
            jmsTemplate.convertAndSend("mailbox", email);
        }
    }
}