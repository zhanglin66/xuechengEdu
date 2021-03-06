package com.xuecheng;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class Consumer05_headers_email {

    private static final String QUEUE_INFORM_EMAIL = "queue_inform_email";
    private static final String EXCHANGE_HEADERS_INFORM = "exchange_headers_inform";


    public static void main(String[] args) throws IOException, TimeoutException {
        //通过连接工厂创建新的连接和mq建立连接
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        //设置虚拟机，一个mq服务可以设置多个虚拟机，每个虚拟机就相当于一个独立的mq
        factory.setVirtualHost("/");
        Connection connection;

        connection = factory.newConnection();
        //创建会话通道,生产者和mq服务所有通信都在channel通道中完成
        Channel channel = connection.createChannel();

        //监听队列
        /**
         * 参数明细
         * 1、queue 队列名称
         * 2、durable 是否持久化，如果持久化，mq重启后队列还在
         * 3、exclusive 是否独占连接，队列只允许在该连接中访问，如果connection连接关闭队列则自动删除,如果将此参数设置true可用于临时队列的创建
         * 4、autoDelete 自动删除，队列不再使用时是否自动删除此队列，如果将此参数和exclusive参数设置为true就可以实现临时队列（队列不用了就自动删除）
         * 5、arguments 参数，可以设置一个队列的扩展参数，比如：可设置存活时间
         */
        channel.queueDeclare(QUEUE_INFORM_EMAIL, true, false, false, null);
        //声明一个交换机
        /**
         * 参数明细:
         * 1.交换机的名称
         * 2.交换机的类型
         * fanout:对应的rabbit的工作模式是publish/subscribe(发布订阅)
         * direct:对应的Routing的工作模式
         * topic:通配符工作模式
         * headers:Header工作模式
         * RPC:远程调用
         */
        //发布订阅模式
        channel.exchangeDeclare(EXCHANGE_HEADERS_INFORM, BuiltinExchangeType.HEADERS);
        //进行交换机和队列绑定
        /**
         * 参数明细：
         * 1.队列名称
         * 2.交换机名称
         * 3.routingKey 路由key，在发布订阅模式中调协为空字符串
         * 作用是交换机根据路由key的值将消息转发到指定的队列中
         */
        Map<String,Object> headers = new HashMap<>();
        headers.put("inform_email", "email");
        channel.queueBind(QUEUE_INFORM_EMAIL, EXCHANGE_HEADERS_INFORM, "",headers);



        //实现消费方法
        DefaultConsumer defaultConsumer = new DefaultConsumer(channel) {
            /**
             * 当接收到消息后此方法将被调用
             * @param consumerTag  消费者标签，用来标识消费者的，在监听队列时设置channel.basicConsume
             * @param envelope 信封，通过envelope
             * @param properties 消息属性
             * @param body 消息内容
             * @throws IOException
             */
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                // System.out.println("consumerTag:" + consumerTag);
                //交换机
                String exchange = envelope.getExchange();
                //消息id，mq在channel中用来标识消息的id，可用于确认消息已接收
                long deliveryTag = envelope.getDeliveryTag();
                //消息内容
                String message = new String(body,"utf-8");
                // System.out.println("exchange:" + exchange);
                // System.out.println("deliveryTag:" + deliveryTag);
                System.out.println("receive message:" + message);
            }
        };
        //监听队列
        //参数：String queue, boolean autoAck, Consumer callback
        /**
         * 参数明细：
         * 1、queue 队列名称
         * 2、autoAck 自动回复，当消费者接收到消息后要告诉mq消息已接收，如果将此参数设置为tru表示会自动回复mq，如果设置为false要通过编程实现回复
         * 3、callback，消费方法，当消费者接收到消息要执行的方法
         */
        channel.basicConsume(QUEUE_INFORM_EMAIL, true, "Geligamesh",defaultConsumer);
    }
}
