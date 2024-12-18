package server.menu;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import cipher.CipherProvider;
import server.config.ServerConfig;
import io.netty.util.internal.StringUtil;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

/**
 * @author yuhao
 * @version 5.11.0
 * @since 2021年05月22日 00:53:00
 */
public class ServerHelper {

    private static final Logger logger = LoggerFactory.getLogger(ServerHelper.class);

    private static final Options OPTIONS = new Options();

    private static CommandLine commandLine;

    public static void useHelp(String[] args) {
        parseArgs(args);
        printHelpMessage();
    }

    private static void parseArgs(String[] args) {
        // program working mode (server(default) or client)
 //       OPTIONS.addOption(Option.builder("o").longOpt("operation_mode").argName("server or client").hasArg(true).type(String.class).desc("program mode server (default) or client").build());
        // address and port
        OPTIONS.addOption(Option.builder("s").longOpt("address").argName("ip:port").hasArg(true).type(String.class).desc("server listen address in format: e.g: ipv4:port , [ipv6]:port").build());
        // password
        OPTIONS.addOption(Option.builder("p").longOpt("password").argName("password").hasArg(true).type(String.class).desc("password").build());
        // method
        OPTIONS.addOption(Option.builder("m").longOpt("method").argName("methodName").hasArg(true).type(String.class).desc("encrypt method. support method: " + CipherProvider.getSupportCiphersNames()).build());

        // number of boss thread
        OPTIONS.addOption(Option.builder("bn").longOpt("boss_number").argName("proc*2").hasArg(true).type(Integer.class).desc("boss thread number").build());
        // number of workers thread
        OPTIONS.addOption(Option.builder("wn").longOpt("workers_number").argName("proc*2").hasArg(true).type(Integer.class).desc("workers thread number").build());
        // client idleTime(second)
        OPTIONS.addOption(Option.builder("ci").longOpt("client_idle").argName("600").hasArg(true).type(Long.class).desc("client idle time(second), default 600").build());
        // remote idleTime(second)
        OPTIONS.addOption(Option.builder("ri").longOpt("remote_idle").argName("600").hasArg(true).type(Long.class).desc("remote idle time(second), default 600").build());
        // set log level
        OPTIONS.addOption(Option.builder("level").longOpt("log_level").argName("INFO").hasArg(true).type(String.class).desc("log level").build());
        // help
        OPTIONS.addOption("h", "usage help");
        OPTIONS.addOption("help", "usage full help");
        try {
            CommandLineParser commandLineParser = new DefaultParser();
            commandLine = commandLineParser.parse(OPTIONS, args);
        } catch (ParseException e) {
            logger.error(e.getMessage() + "\n" + getShortHelpString());
            System.exit(0);
        }
    }

    private static void printHelpMessage() {
        if (commandLine.hasOption("h")) {
            logger.info("\n" + getShortHelpString());
            System.exit(1);
        }
        if (commandLine.hasOption("help")) {
            logger.info("\n" + getFullHelpString());
            System.exit(1);
        }
        // address
        if (StringUtil.isNullOrEmpty(commandLine.getOptionValue("s"))) {
            logger.error("server address is required\n" + getShortHelpString());
            System.exit(1);
        }
        ServerConfig.serverConfig.setLocalAddress(commandLine.getOptionValue("s"));
        // password
        if (StringUtil.isNullOrEmpty(commandLine.getOptionValue("p"))) {
            logger.error("server password is required\n" + getShortHelpString());
            System.exit(1);
        }
        ServerConfig.serverConfig.setPassword(commandLine.getOptionValue("p"));
        // method
        if (StringUtil.isNullOrEmpty(commandLine.getOptionValue("m"))) {
            logger.error("method is required\n" + getShortHelpString());
            System.exit(1);
        }
        ServerConfig.serverConfig.setMethod(commandLine.getOptionValue("m"));

        // boss thread number
        String bossThreadNumber = StringUtil.isNullOrEmpty(commandLine.getOptionValue("bn")) ? String.valueOf(Runtime.getRuntime().availableProcessors() * 2) : commandLine.getOptionValue("bn");
        ServerConfig.serverConfig.setBossThreadNumber(Integer.parseInt(bossThreadNumber));
        // workers thread number
        String workersThreadNumber = StringUtil.isNullOrEmpty(commandLine.getOptionValue("wn")) ? String.valueOf(Runtime.getRuntime().availableProcessors() * 2) : commandLine.getOptionValue("wn");
        ServerConfig.serverConfig.setWorkersThreadNumber(Integer.parseInt(workersThreadNumber));
        // client readIdle time(second)
        String clientIdleTime = StringUtil.isNullOrEmpty(commandLine.getOptionValue("ci")) ? String.valueOf(600) : commandLine.getOptionValue("ci");
        ServerConfig.serverConfig.setClientIdle(Long.parseLong(clientIdleTime));
        // remote readIdle time(second)
        String remoteIdleTime = StringUtil.isNullOrEmpty(commandLine.getOptionValue("ri")) ? String.valueOf(600) : commandLine.getOptionValue("ri");
        ServerConfig.serverConfig.setRemoteIdle(Long.parseLong(remoteIdleTime));
        // log level
        String levelName = commandLine.getOptionValue("level");
        if (StringUtil.isNullOrEmpty(levelName)) {
            Level level = Level.toLevel(levelName, Level.INFO);
            logger.info("set log level to " + level.toString());

            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger("ROOT");
            rootLogger.setLevel(level);
            if (Level.toLevel(levelName).levelInt < Level.INFO.levelInt) {
                PatternLayoutEncoder ple = new PatternLayoutEncoder();
                ple.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");
                ple.setContext(loggerContext);
                ple.start();

                ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
                consoleAppender.setEncoder(ple);
                consoleAppender.setName("STDOUT");
                consoleAppender.setContext(loggerContext);
                consoleAppender.start();

                rootLogger.detachAppender("STDOUT");
                rootLogger.addAppender(consoleAppender);
            }
        }
    }

    /**
     * get string of short  help usage
     *
     * @return help string
     */
    private static String getShortHelpString() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setOptionComparator(null);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintWriter printWriter = new PrintWriter(byteArrayOutputStream);

        Options shortOptions = new Options();
 //       shortOptions.addOption(OPTIONS.getOption("o"));
        shortOptions.addOption(OPTIONS.getOption("s"));
        shortOptions.addOption(OPTIONS.getOption("p"));
        shortOptions.addOption(OPTIONS.getOption("m"));
        shortOptions.addOption(OPTIONS.getOption("h"));
        shortOptions.addOption(OPTIONS.getOption("help"));

        helpFormatter.printHelp(printWriter, HelpFormatter.DEFAULT_WIDTH * 2, "java -jar shadowsocks-server.jar -h", null,
                shortOptions, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, null);
        printWriter.flush();
        String result = byteArrayOutputStream.toString();
        printWriter.close();
        return result;
    }

    /**
     * get string of help usage
     *
     * @return help string
     */
    private static String getFullHelpString() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setOptionComparator(null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintWriter printWriter = new PrintWriter(byteArrayOutputStream);
        helpFormatter.printHelp(printWriter, HelpFormatter.DEFAULT_WIDTH * 2, "java -jar shadowsocks-server.jar -help", null,
                OPTIONS, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, null);
        printWriter.flush();
        String result = byteArrayOutputStream.toString();
        printWriter.close();
        return result;
    }
}
