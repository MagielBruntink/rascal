package org.rascalmpl.library.experiments.Compiler.Commands;

import java.io.IOException;
import java.net.URISyntaxException;

import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.NoSuchRascalFunction;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.RascalExecutionContext;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.RascalExecutionContextBuilder;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.java2rascal.Java2Rascal;
import org.rascalmpl.library.lang.rascal.boot.IKernel;
import org.rascalmpl.value.IValueFactory;
import org.rascalmpl.values.ValueFactoryFactory;

public class BootstrapRascalParser {

	/**
	 * This command is used by Bootstrap only.
	 *  
	 * @param args	list of command-line arguments
	 * @throws NoSuchRascalFunction 
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public static void main(String[] args) {
	    try {
	        IValueFactory vf = ValueFactoryFactory.getValueFactory();
	        CommandOptions cmdOpts = new CommandOptions("generateParser");
	        cmdOpts
	        .locsOption("src")		
	        .locsDefault(cmdOpts.getDefaultStdlocs().isEmpty() ? vf.list(cmdOpts.getDefaultStdlocs()) : cmdOpts.getDefaultStdlocs())
	        .respectNoDefaults()
	        .help("Add (absolute!) source location, use multiple --src arguments for multiple locations")
	        
	        .locOption("boot")		
	        .locDefault(cmdOpts.getDefaultBootLocation())
	        .help("Rascal boot directory")
	        
            .boolOption("trace") 		
            .help("Print Rascal functions during execution of compiler")
            
            .boolOption("profile") 		
            .help("Profile execution of compiler")
           
            .boolOption("verbose") 		
            .help("Make the compiler verbose")

	        .noModuleArgument()
	        .handleArgs(args);

	        RascalExecutionContext rex = RascalExecutionContextBuilder.normalContext(ValueFactoryFactory.getValueFactory(), cmdOpts.getCommandLocOption("boot"))
	                .customSearchPath(cmdOpts.getPathConfig().getRascalSearchPath())
	                .setTrace(cmdOpts.getCommandBoolOption("trace"))
	                .setProfile(cmdOpts.getCommandBoolOption("profile"))
	                .setVerbose(cmdOpts.getCommandBoolOption("verbose"))
	                .build();

	        //Kernel kernel = new Kernel(vf, rex, cmdOpts.getCommandLocOption("boot"));
	        IKernel kernel = Java2Rascal.Builder.bridge(vf, cmdOpts.getPathConfig(), IKernel.class).build();

	        kernel.bootstrapRascalParser(cmdOpts.getCommandLocsOption("src"));
	    }
		catch (Throwable e) {
		    e.printStackTrace();
		    System.exit(1);
		}
	}
}
