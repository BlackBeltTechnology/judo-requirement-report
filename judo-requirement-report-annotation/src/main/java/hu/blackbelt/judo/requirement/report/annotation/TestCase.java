package hu.blackbelt.judo.requirement.report.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.SOURCE )
@Target( value = { ElementType.METHOD })
public @interface TestCase {
    String value();
}
