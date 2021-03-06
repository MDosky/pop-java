package popjava.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface POPConfig {

	public enum Type{
		URL,
		CONNECTION,
		CONNECTION_PWD,
		ACCESS_POINT
	}
	
	Type value();
	
}
