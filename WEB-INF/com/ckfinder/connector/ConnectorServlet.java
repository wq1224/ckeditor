/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletConfig
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServlet
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.ckfinder.connector;

import com.ckfinder.connector.ServletContextFactory;
import com.ckfinder.connector.configuration.Configuration;
import com.ckfinder.connector.configuration.ConfigurationFactory;
import com.ckfinder.connector.configuration.Events;
import com.ckfinder.connector.configuration.IConfiguration;
import com.ckfinder.connector.data.BeforeExecuteCommandEventArgs;
import com.ckfinder.connector.data.EventArgs;
import com.ckfinder.connector.errors.ConnectorException;
import com.ckfinder.connector.handlers.command.Command;
import com.ckfinder.connector.handlers.command.CopyFilesCommand;
import com.ckfinder.connector.handlers.command.CreateFolderCommand;
import com.ckfinder.connector.handlers.command.DeleteFilesCommand;
import com.ckfinder.connector.handlers.command.DeleteFolderCommand;
import com.ckfinder.connector.handlers.command.DownloadFileCommand;
import com.ckfinder.connector.handlers.command.ErrorCommand;
import com.ckfinder.connector.handlers.command.FileUploadCommand;
import com.ckfinder.connector.handlers.command.GetFilesCommand;
import com.ckfinder.connector.handlers.command.GetFoldersCommand;
import com.ckfinder.connector.handlers.command.IPostCommand;
import com.ckfinder.connector.handlers.command.InitCommand;
import com.ckfinder.connector.handlers.command.MoveFilesCommand;
import com.ckfinder.connector.handlers.command.QuickUploadCommand;
import com.ckfinder.connector.handlers.command.RenameFileCommand;
import com.ckfinder.connector.handlers.command.RenameFolderCommand;
import com.ckfinder.connector.handlers.command.ThumbnailCommand;
import com.ckfinder.connector.handlers.command.XMLCommand;
import com.ckfinder.connector.handlers.command.XMLErrorCommand;
import com.ckfinder.connector.utils.AccessControlUtil;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ConnectorServlet
extends HttpServlet {
    private Exception startException;
    private static final long serialVersionUID = 2960665641425153638L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        this.getResponse(request, response, false);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        this.getResponse(request, response, true);
    }

    private void getResponse(HttpServletRequest request, HttpServletResponse response, boolean post) throws ServletException {
        if (this.startException != null && Boolean.valueOf(this.getServletConfig().getInitParameter("debug")).booleanValue()) {
            throw new ServletException((Throwable)this.startException);
        }
        String command = request.getParameter("command");
        IConfiguration configuration = null;
        try {
            configuration = ConfigurationFactory.getInstace().getConfiguration(request);
            if (configuration == null) {
                throw new Exception("Configuration wasn't initialized correctly. Check server logs.");
            }
        }
        catch (Exception e) {
            if (Boolean.valueOf(this.getServletConfig().getInitParameter("debug")).booleanValue()) {
                Logger.getLogger(ConnectorServlet.class.getName()).log(Level.SEVERE, "Configuration wasn't initialized correctly. Check server logs.", e);
            }
            throw new ServletException((Throwable)e);
        }
        try {
            boolean isNativeCommand;
            if (command == null || command.equals("")) {
                throw new ConnectorException(10, false);
            }
            configuration.setDebugMode(Boolean.valueOf(this.getServletConfig().getInitParameter("debug")));
            if (CommandHandlerEnum.contains(command.toUpperCase())) {
                isNativeCommand = true;
                CommandHandlerEnum cmd = CommandHandlerEnum.valueOf(command.toUpperCase());
                if ((cmd.getCommand() instanceof IPostCommand || post) && !CommandHandlerEnum.FILEUPLOAD.equals((Object)cmd) && !CommandHandlerEnum.QUICKUPLOAD.equals((Object)cmd)) {
                    this.checkPostRequest(request);
                }
            } else {
                isNativeCommand = false;
            }
            BeforeExecuteCommandEventArgs args = new BeforeExecuteCommandEventArgs();
            args.setCommand(command);
            args.setRequest(request);
            args.setResponse(response);
            if (configuration.getEvents() != null) {
                if (configuration.getEvents().run(Events.EventTypes.BeforeExecuteCommand, args, configuration)) {
                    if (!isNativeCommand) {
                        command = null;
                    }
                    this.executeNativeCommand(command, request, response, configuration, isNativeCommand);
                }
            } else {
                if (!isNativeCommand) {
                    command = null;
                }
                this.executeNativeCommand(command, request, response, configuration, isNativeCommand);
            }
        }
        catch (IllegalArgumentException e) {
            if (Boolean.valueOf(this.getServletConfig().getInitParameter("debug")).booleanValue()) {
                Logger.getLogger(ConnectorServlet.class.getName()).log(Level.SEVERE, "Couldn't execute native command.", e);
                response.reset();
                throw new ServletException((Throwable)e);
            }
            this.handleError(new ConnectorException(10, false), configuration, request, response, command);
        }
        catch (ConnectorException e /* !! */ ) {
            if (Boolean.valueOf(this.getServletConfig().getInitParameter("debug")).booleanValue()) {
                Logger.getLogger(ConnectorServlet.class.getName()).log(Level.SEVERE, e /* !! */ .getErrorMessage(), e /* !! */ .getException() != null ? e /* !! */ .getException() : e /* !! */ );
                response.reset();
                throw new ServletException((Throwable)e /* !! */ .getException());
            }
            this.handleError(e /* !! */ , configuration, request, response, command);
        }
    }

    private void executeNativeCommand(String command, HttpServletRequest request, HttpServletResponse response, IConfiguration configuration, boolean isNativeCommand) throws IllegalArgumentException, ConnectorException {
        if (!isNativeCommand) {
            throw new ConnectorException(10, false);
        }
        CommandHandlerEnum cmd = CommandHandlerEnum.valueOf(command.toUpperCase());
        cmd.execute(request, response, configuration, this.getServletContext(), new Object[0]);
    }

    private void checkPostRequest(HttpServletRequest request) throws ConnectorException {
        if (request.getParameter("CKFinderCommand") == null || !request.getParameter("CKFinderCommand").equals("true")) {
            throw new ConnectorException(109, true);
        }
    }

    private void handleError(ConnectorException e, IConfiguration configuration, HttpServletRequest request, HttpServletResponse response, String currentCommand) throws ServletException {
        try {
            if (currentCommand != null && !currentCommand.equals("")) {
                Command command = CommandHandlerEnum.valueOf(currentCommand.toUpperCase()).getCommand();
                if (command instanceof XMLCommand) {
                    CommandHandlerEnum.XMLERROR.execute(request, response, configuration, this.getServletContext(), new Object[]{e});
                } else {
                    CommandHandlerEnum.ERROR.execute(request, response, configuration, this.getServletContext(), new Object[]{e});
                }
            } else {
                CommandHandlerEnum.XMLERROR.execute(request, response, configuration, this.getServletContext(), new Object[]{e});
            }
        }
        catch (Exception e1) {
            throw new ServletException((Throwable)e1);
        }
    }

    public void init() throws ServletException {
        IConfiguration configuration2;
        IConfiguration configuration2;
        ServletContextFactory.setServletContext(this.getServletContext());
        try {
            Class clazz;
            String className = this.getServletConfig().getInitParameter("configuration");
            configuration2 = className != null ? ((clazz = Class.forName(className)).getConstructor(ServletConfig.class) != null ? (IConfiguration)clazz.getConstructor(ServletConfig.class).newInstance(new Object[]{this.getServletConfig()}) : (IConfiguration)clazz.newInstance()) : new Configuration(this.getServletConfig());
        }
        catch (Exception e) {
            Logger.getLogger(ConnectorServlet.class.getName()).log(Level.SEVERE, "Couldn't initialize custom configuration. Rolling back to the default one.", e);
            configuration2 = new Configuration(this.getServletConfig());
        }
        try {
            configuration2.init();
            AccessControlUtil.getInstance().loadConfiguration(configuration2);
        }
        catch (Exception e) {
            if (Boolean.valueOf(this.getServletConfig().getInitParameter("debug")).booleanValue()) {
                Logger.getLogger(ConnectorServlet.class.getName()).log(Level.SEVERE, "Couldn't initialize configuration object.", e);
            }
            this.startException = e;
            configuration2 = null;
        }
        ConfigurationFactory.getInstace().setConfiguration(configuration2);
    }

    private static enum CommandHandlerEnum {
        INIT(new InitCommand()),
        GETFOLDERS(new GetFoldersCommand()),
        GETFILES(new GetFilesCommand()),
        THUMBNAIL(new ThumbnailCommand()),
        DOWNLOADFILE(new DownloadFileCommand()),
        CREATEFOLDER(new CreateFolderCommand()),
        RENAMEFILE(new RenameFileCommand()),
        RENAMEFOLDER(new RenameFolderCommand()),
        DELETEFOLDER(new DeleteFolderCommand()),
        COPYFILES(new CopyFilesCommand()),
        MOVEFILES(new MoveFilesCommand()),
        DELETEFILES(new DeleteFilesCommand()),
        FILEUPLOAD(new FileUploadCommand()),
        QUICKUPLOAD(new QuickUploadCommand()),
        XMLERROR(new XMLErrorCommand()),
        ERROR(new ErrorCommand());
        
        private Command command;
        private static final HashSet<String> enumValues;

        private CommandHandlerEnum(Command command1) {
            this.command = command1;
        }

        private static void setEnums() {
            for (CommandHandlerEnum enumValue : CommandHandlerEnum.values()) {
                enumValues.add(enumValue.name());
            }
        }

        public static boolean contains(String enumValue) {
            if (enumValues.isEmpty()) {
                CommandHandlerEnum.setEnums();
            }
            for (String value : enumValues) {
                if (!value.equals(enumValue)) continue;
                return true;
            }
            return false;
        }

        private /* varargs */ void execute(HttpServletRequest request, HttpServletResponse response, IConfiguration configuration, ServletContext sc, Object ... params) throws ConnectorException {
            Command com = null;
            try {
                com = (Command)this.command.getClass().newInstance();
            }
            catch (IllegalAccessException e1) {
                throw new ConnectorException(10);
            }
            catch (InstantiationException e1) {
                throw new ConnectorException(10);
            }
            if (com == null) {
                throw new ConnectorException(10);
            }
            com.runCommand(request, response, configuration, params);
        }

        public Command getCommand() {
            return this.command;
        }

        static {
            enumValues = new HashSet();
        }
    }

}

