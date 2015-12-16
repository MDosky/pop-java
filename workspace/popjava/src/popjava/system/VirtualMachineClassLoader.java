package popjava.system;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

public class VirtualMachineClassLoader extends URLClassLoader{

	public VirtualMachineClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }
 
    public VirtualMachineClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }
 
    public VirtualMachineClassLoader(URL[] urls) {
        super(urls);
    }
 
    public VirtualMachineClassLoader() {
        super(new URL[0]);
    }
    
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
    	Class<?> loadedClass = findLoadedClass(name);
    	 
        // Nope, try to load it
        if (loadedClass == null) {
            try {
                // Ignore parent delegation and just try to load locally
                loadedClass = findClass(name);
            }
            catch (ClassNotFoundException e) {
                // Swallow - does not exist locally
            }

            // If not found, just use the standard URLClassLoader (which follows normal parent delegation)
            if (loadedClass == null) {
                // throws ClassNotFoundException if not found in delegation hierarchy at all
                loadedClass = super.loadClass(name);
            }
        }
        return loadedClass;
    }
 
    @Override
    public URL getResource(final String name) {
        final URL resource = findResource(name);
 
        if (resource != null) {
            return resource;
        }
 
        return super.getResource(name);
    }
}
