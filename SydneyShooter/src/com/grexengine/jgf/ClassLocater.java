package com.grexengine.jgf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//import org.apache.log4j.*;

/**
 * An essential part of Java - locates any Class, anywhere.
 * </P>
 * This class should have been part of the JDK for the last 7 years, but Sun hasn't
 * added it, so we did it instead :).
 * <P>
 * No static methods since people are already using this in environments where
 * they need multiple separately configured copies running in parallel. Sun's JVM
 * design (caching classloaders) ensure that cached class data is automatically
 * shared between instances; it could be made faster by storing an internal DB
 * rather than re-instantiating, but the time-savings are minuscule (might save
 * some milliseconds if you have 1000 classes).
 * <h3>Usage Tips</h3>
 * If you are using this to automatically find all plugins that your users have for
 * your API then the easiest thing to do is declare a package that plugins are in.
 * Note: you could also declare a naming convention, as many open-source
 * projects have done when writing poor alternatives to this method. This is
 * bad practice, since java already has a naming-convention system - packages -
 * and we can easily use that - but it required more coding to make it work.
 * There are cases where you cannot use the packages this way (though personally
 * I'd recommend you re-think your design in that case), and in those cases you
 * can easily use a class-naming convention instead. So, everyone should be
 * happy.
 * <P>
 * If you reserve a package for plugins, e.g. declare that all plugins must be in
 * package "org.javagamesfactory.plugins", then you simply pass something like
 * "org\.javagamesfactory\.plugins\..*" in (regex meaning "all things in that package).
 * This class will actually find all things in that pacakge <i>even if they are in
 * different copies of that package, in different JAR files, or different directories</i>.
 * <P>
 * To use a naming convention, e.g. all plugin class names start with the text "PLUGIN"
 * you would do something like: ".*\.PLUGIN.*".
 * <p>
 * In all cases, note the fact that regex's have special meaning for dot, so you have to
 * escape it when you just mean a full-stop. Read the java API docs for java.util.regex
 * for more information
 *
 * @see java.util.regex.Pattern
 */
public class ClassLocater
{
//  protected static Logger logger = Logger.getLogger( "jgf.classlocater");
  protected LinkedList skipPrefixes = new LinkedList();

  /**
   * Finds all classes that implement or extend a given class name, and
   * instantiates precisely one copy of each
   *
   * @param className fully qualified class or interface to find subclasses of,
   * e.g. "java.lang.String"
   * @param skipPrefixes prefixes of fully qualified packages or class names to
   * completely ignore (i.e. not bother to check), making it faster, e.g. "java.", "com.sun"
   * @return instantiated objects
   */
  public static List instantiateOneOfEach( String className, String[] skipPrefixes )
  {
    Class[] classes = null;
    LinkedList instances = new LinkedList();

    try
    {
      ClassLocater locater = new ClassLocater();

      for( int i = 0; i < skipPrefixes.length; i++ )
      {
        locater.addSkipPrefix( skipPrefixes[i] );
      }
      classes = locater.getSubclassesOf( Class.forName( className ) );

	/*logger.info("Found "+classes.length+" classes that implement "+className+"...");
      if( logger.isDebugEnabled() )
        for( int i = 0; i < classes.length; i++ )
        {
          logger.debug( "Found "+classes[i].getName()+" that implements "+className+"...");
        }
        */
    }
    catch( Exception e )
    {
      //logger.error( "Attempting to find "+className+" implementers", e);
    }

    // Iterate through all, instantiating them
    //logger.debug( "Instantiating each class");
    for( int i = 0; i < classes.length; i++ )
    {
      try
      {
        Object o = classes[i].newInstance();
        instances.add( o );
      }
      catch( Throwable e )
      {
        //logger.error( "Failed to process: "+classes[i].getName(), e);
      }
    }

    return instances;
  }

  /**
   * Automatically adds sun's classes, the java library classes, and the Apache
   * log4j classes (a lib used by ClassLocater!) to the skip list; it's very unlikely
   * that you're trying to locate any of these!
   */
  public ClassLocater()
  {
    addSkipPrefix( "org.apache.log4j." );
    addSkipPrefix( "com.sun." );
    addSkipPrefix( "java" );
  }

  /**
   * Adds a prefix for classes (and packages) to completely ignore, based on their
   * package + class name.
   * <p>
   * For example, "org.apache.log4j".
   * <P>
   * The advantage of this method is that you don't have to bother with regex
   * syntax. Also, it is remembered between calls to getSubclassesOf - so it's
   * useful if you know you never care about certain packages.
   *
   * @param s prefix of fully qualified class names to ignore
   */
  public void addSkipPrefix( String s )
  {
    skipPrefixes.add( s );
  }

  /**
  * Find all instances of the given <code>Class</code> or interface by
  * loading all classes on the class path.
  * <P>
  * Delegates to the other version, but passing in ".*" as the regex, i.e.
  * "anything at all"
  *
  * @param targetType the superclass of all returned classes.
  * @return an array of all subclasses of <code>targetType</code>
  */
  public Class[] getSubclassesOf( Class targetType )
  {
    return getSubclassesOf( targetType, ".*" );
  }

  /**
  * Find all subclasses of the given <code>Class</code> or interface by
  * loading only those classes with names that match the given regular
  * expression.
  * <P>
  * Once all classes have been checked, it will output at WARN a list of all the classes
  * that were referenced by other classes but are not installed in the classpath. This
  * can be incredibly useful - it catches situations where e.g. you thought a class
  * was on the classpath but you put it in the wrong directory etc.
  * <P>
  * It can also be very annoying because java uses dynamic linking so it is LEGAL
  * for many classes to be missing, just so long as you never use them at runtime.
  * Because this class tries to use *every* class, it triggers errors on lots that you
  * don't care about - use addSkipPrefix( class or package you dont use even though
  * its on the classpath ) and they will be skipped (i.e. not even examined by this
  * method).
  * <P>
  * OR improve your regex so that it is more selective about the packages where
  * your classes could conceivable be located!
  *
  * @param targetType the superclass of all returned classes.
  * @param regex a regular expression that will match with every subclass
  * @return an array of all subclasses of <code>targetType</code>
  */
  public Class[] getSubclassesOf( Class targetType, String regex )
  {
    //logger.info( "Looking for all classes with names matching regex = "+regex+" and which are subtypes of "+targetType.getName() );
    StringBuffer sbSkips = new StringBuffer();
    for( Iterator i2 = skipPrefixes.iterator(); i2.hasNext(); )
    {
      sbSkips.append( i2.next().toString() +"*" );
      if( i2.hasNext() )
       sbSkips.append( ", " );
    }
    //logger.warn( "...unless they match: "+sbSkips.toString() );

    LinkedList matches = new LinkedList();
    HashMap missingRequiredClasses = new HashMap(); // maps class name to list of classes that needed it
    //logger.debug( "Creating ClassPath object to do class search...");

    ClassPath cp = new ClassPath();
    //logger.debug( "Iterating through all classes in ClassPath...");

    for( Iterator iter = cp.getAllClassNames().iterator(); iter.hasNext(); )
    {
      String className = (String)iter.next();

      boolean skip = false;
      for( Iterator i2 = skipPrefixes.iterator(); i2.hasNext(); )
      {
        String prefix = (String) i2.next();
        if( className.startsWith( prefix ) )
        {
          //logger.info( "Skipping class = "+className+" because it has a prefix of "+prefix );
          skip = true;
          break;
        }
      }
      if( skip )
        continue;

      //logger.debug( "Processing class: "+className);
      if ( className.matches( regex )
      && !className.equals( targetType.getName() ) )
      {
        //logger.debug( "...matches regex; instantiating and checking type");
        Class clazz = null;
        try
        {
          clazz = Class.forName( className );
        }
        /*catch (ClassNotFoundException cnfx )
        {
          continue;
        }*/
        catch (NoClassDefFoundError cnfx )
        {
          /*
           * This is ridiculous. Please, everyone, ask sun to add a "getMissingClass()"
           * method to NoClassDefFoundError: Sun, you have had TEN YEARS to fix
           * this!
           */
          if( cnfx.getMessage() == null )
          {
            //logger.warn( "NoClassDefFoundError but Sun didn't fill-in the message; no idea which class it was; ignoring it and moving on", cnfx );
            continue;
          }
          String missingClassName = cnfx.getMessage().replace( '/', '.' );

          LinkedList misses = (LinkedList) missingRequiredClasses.get( missingClassName );
          if( misses == null )
          {
            misses = new LinkedList();
            missingRequiredClasses.put( missingClassName, misses );
          }

          misses.add( className );

          continue;
        }
        catch( UnsatisfiedLinkError cnfx )
        {
          continue;
        }
        catch( Throwable t )
        {
          //logger.warn( "Unexpected error - REMOVING this class ("+className+") without checking it", t );
          continue;
        }
        finally
        {
          if ( clazz != null && targetType.isAssignableFrom( clazz ) )
          {
            //logger.info( className+" matches and is correct type; adding to results");
            matches.add( clazz );
          }
        }

      }

    }

    if( missingRequiredClasses.size() >  0 )
    {
      /*logger.warn( "The following classes were needed by some of the classes I found, but could not themselves be found."
        + "Check you have the required libraries, that they are on the classpath, and that all JAR's are in your manifest as needed");
      logger.warn( "If you don't care about some of the classes that used these missing classes, add the users to the skip list and you will get no errors from them");
      */
      for( Iterator iter = missingRequiredClasses.keySet().iterator(); iter.hasNext(); )
      {
        String className = (String) iter.next();
        LinkedList neededBy = (LinkedList) missingRequiredClasses.get( className );
        StringBuffer sb = new StringBuffer();
        for( Iterator iterator = neededBy.iterator(); iterator.hasNext(); )
        {
          String referencingClass = (String) iterator.next();
          sb.append( referencingClass );
          if( iterator.hasNext() )
            sb.append( ", ");
        }
        //logger.warn( "class: "+className+" was needed by class"+(neededBy.size() == 1 ? "" : "es")+": "+sb );
      }
    }

    return (Class[])matches.toArray( new Class[0] );

  }
}
