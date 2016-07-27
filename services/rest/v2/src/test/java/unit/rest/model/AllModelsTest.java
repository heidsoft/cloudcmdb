package unit.rest.model;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.cmdbuild.service.rest.v2.model.Model;
import org.junit.Test;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.common.reflect.Reflection;

public class AllModelsTest {

	@Test
	public void allModelsMustHaveOneOnlyAndNotPublicContructor() throws Exception {
		final ClassLoader classLoader = AllModelsTest.class.getClassLoader();
		final String targetPackage = Reflection.getPackageName(Model.class);
		for (final ClassInfo classInfo : ClassPath.from(classLoader).getTopLevelClasses(targetPackage)) {
			final String name = classInfo.getName();
			if (name.endsWith("package-info")) {
				continue;
			}

			final Class<?> clazz = Class.forName(name);
			if (clazz.isInterface()) {
				continue;
			}
			final Constructor<?>[] constructors = clazz.getDeclaredConstructors();
			assertThat("missing constructors for class" + name, constructors.length, greaterThan(0));
			for (final Constructor<?> constructor : constructors) {
				assertThat("invalid modifier for constructor " + constructor + " for class " + clazz,
						Modifier.isPublic(constructor.getModifiers()), equalTo(false));
			}
		}
	}

}
