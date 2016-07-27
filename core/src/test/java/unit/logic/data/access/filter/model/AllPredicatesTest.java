package unit.logic.data.access.filter.model;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.cmdbuild.logic.data.access.filter.model.Predicate;
import org.junit.Test;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.common.reflect.Reflection;

public class AllPredicatesTest {

	@Test
	public void allElementsMustHaveOneOnlyAndNotPublicContructor() throws Exception {
		final ClassLoader classLoader = AllPredicatesTest.class.getClassLoader();
		final String targetPackage = Reflection.getPackageName(Predicate.class);
		for (final ClassInfo classInfo : ClassPath.from(classLoader).getTopLevelClasses(targetPackage)) {
			final String name = classInfo.getName();
			final Class<?> clazz = Class.forName(name);
			if (!Predicate.class.isAssignableFrom(clazz)) {
				continue;
			}
			if (Modifier.isAbstract(clazz.getModifiers())) {
				continue;
			}
			final Constructor<?>[] constructors = clazz.getDeclaredConstructors();
			assertThat(constructors.length, lessThanOrEqualTo(1));
			final Constructor<?> onlyConstructor = constructors[0];
			assertThat("invalid modifier at class " + name, Modifier.isPublic(onlyConstructor.getModifiers()),
					equalTo(false));
		}
	}

}
