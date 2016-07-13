package main;

import org.apache.commons.lang.SystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class CommandProcessor {

    private static final String MSG_COMMAND_NOT_FOUND = "Command not found";
    private static final String MSG_DELIM = "==========================================";

    private Map<String, Command> commands;

    private String consoleEncoding;

    public CommandProcessor(String consoleEncoding) {
        commands = new TreeMap<>();
        Command cmd = new HelpCommand();
        commands.put(cmd.getName(), cmd);
        cmd = new ListDirCommand();
        commands.put(cmd.getName(), cmd);
        cmd = new PrintWorkingDirCommand();
        commands.put(cmd.getName(), cmd);
        cmd = new ProcessesCommand();
        commands.put(cmd.getName(), cmd);
        cmd = new ExitCommand();
        commands.put(cmd.getName(), cmd);
        this.consoleEncoding = consoleEncoding;
    }

    public void execute() {
        Context c = new Context();
        c.currentDirectory = new File(".").getAbsoluteFile();
        boolean result = true;
        Scanner scanner = new Scanner(System.in, consoleEncoding);
        do {
            System.out.print("> ");
            String fullCommand = scanner.nextLine();
            ParsedCommand pc = new ParsedCommand(fullCommand);
            if (pc.command == null || "".equals(pc.command)) {
                continue;
            }
            Command cmd = commands.get(pc.command.toUpperCase());
            if (cmd == null) {
                System.out.println(MSG_COMMAND_NOT_FOUND);
                continue;
            }
            result = cmd.execute(c, pc.args);
        } while (result);
    }

    public static void main(String[] args) {
        CommandProcessor cp = new CommandProcessor("utf-8");
        cp.execute();
    }


    class ParsedCommand {

        String command;

        String[] args;

        public ParsedCommand(String line) {
            String parts[] = line.split(" ");
            if (parts != null) {
                command = parts[0];
                if (parts.length > 1) {
                    args = new String[parts.length - 1];
                    System.arraycopy(parts, 1, args, 0, args.length);
                }
            }
        }
    }

    interface Command {

        boolean execute(Context context, String... args);

        void printHelp();

        String getName();

        String getDescription();
    }

    class Context {

        private File currentDirectory;

    }

    class HelpCommand implements Command {

        @Override
        public boolean execute(Context context, String... args) {
            if (args == null) {
                System.out.println("Avaliable commands:\n" + MSG_DELIM);
                for (Command cmd : commands.values()) {
                    System.out.println(cmd.getName() + ": " + cmd.getDescription());
                }
                System.out.println(MSG_DELIM);
            } else {
                for (String cmd : args) {
                    System.out.println("Help for command " + cmd + ":\n" + MSG_DELIM);
                    Command command = commands.get(cmd.toUpperCase());
                    if (command == null) {
                        System.out.println(MSG_COMMAND_NOT_FOUND);
                    } else {
                        command.printHelp();
                    }
                    System.out.println(MSG_DELIM);
                }
            }
            return true;
        }

        @Override
        public void printHelp() {
            System.out.println(getDescription());
        }

        @Override
        public String getName() {
            return "HELP";
        }

        @Override
        public String getDescription() {
            return "Prints list of available commands";
        }
    }

    class ListDirCommand implements Command {

        @Override
        public void printHelp() {
            System.out.println(getDescription());
        }

        @Override
        public boolean execute(Context context, String... args) {
            if (args == null) {
                printFile(context.currentDirectory);
            } else {
                for (String path : args) {
                    System.out.println("List of files for " + path + "\n" + MSG_DELIM);
                    File f = new File(path);
                    if(f.exists()) {

                        printFile(f);
                        System.out.println(MSG_DELIM);
                    }
                    else {
                        System.err.println("Bad file path\n" + MSG_DELIM);
                    }

                }

            }
            return true;
        }

        @Override
        public String getName() {
            return "LS";
        }

        private void printFile(File dir) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if(f.isDirectory()) System.out.print("<DIR>");
                    else System.out.print("<FILE>");
                    System.out.println("\t" + f.getName());
                }
            }
        }

        @Override
        public String getDescription() {
            return "Shows you the files in your current directory.";
        }
    }

    class ExitCommand implements Command {
        @Override
        public boolean execute(Context context, String... args) {
            System.out.println("Finishing command processor... done.");
            return false;
        }

        @Override
        public void printHelp() {
            System.out.println(getDescription());
        }

        @Override
        public String getName() {
            return "EXIT";
        }

        @Override
        public String getDescription() {
            return "Exits from command processor";
        }
    }

    class ProcessesCommand implements Command {

        @Override
        public boolean execute(Context context, String... args) {
            try {
                Process p = null;
                if (SystemUtils.IS_OS_UNIX) {
                     p = Runtime.getRuntime().exec("ps -e");
                }
                if(SystemUtils.IS_OS_WINDOWS) {
                     p = Runtime.getRuntime().exec
                            (System.getenv("windir") +"\\system32\\"+"tasklist.exe");
                }
                String line;

                BufferedReader input =
                        new BufferedReader(new InputStreamReader(p.getInputStream()));

                while ((line = input.readLine()) != null) {
                    if (args == null) {
                        System.out.println(line);
                    }
                    else {
                        if(line.replaceAll("\\d","").toLowerCase().contains(args[0].toLowerCase()) &&
                                !args[0].equals("Services") &&  !args[0].equals("Console"))
                            System.out.println(line);
                    }
                }
                input.close();
            } catch (Exception err) {
                err.printStackTrace();
            }
            return true;
        }

        @Override
        public void printHelp() {
            System.out.println(getDescription());
        }

        @Override
        public String getName() {
            return "PS";
        }

        @Override
        public String getDescription() {
            return "Allows you to view all the processes running on the machine";
        }
    }

    class PrintWorkingDirCommand implements Command{
        @Override
        public boolean execute(Context context, String... args) {
            if (args == null) {
                System.out.println("Working directory: \n" + MSG_DELIM);
                System.out.println(context.currentDirectory);
                System.out.println(MSG_DELIM);
            }
            return true;
        }

        @Override
        public void printHelp() {
            System.out.println(getDescription());
        }

        @Override
        public String getName() {
            return "PWD";
        }

        @Override
        public String getDescription() {
            return "Allows you to know the directory in which you're located";
        }
    }


}