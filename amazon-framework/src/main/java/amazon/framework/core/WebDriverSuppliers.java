package amazon.framework.core;

import org.junit.experimental.theories.ParameterSignature;
import org.junit.experimental.theories.ParameterSupplier;
import org.junit.experimental.theories.ParametersSuppliedBy;
import org.junit.experimental.theories.PotentialAssignment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class WebDriverSuppliers {

	/**
	 * Execute the test with multiple browsers while using the JUnit Theories runtime
	 *
	 * @author Nicolas Rémond (nre)
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@ParametersSuppliedBy(AllWebDriversSupplier.class)
	public @interface AllWebDrivers {
	}
	public static class AllWebDriversSupplier extends ParameterSupplier {
		@Override
		public List<PotentialAssignment> getValueSources(final ParameterSignature sig) {
			final List<PotentialAssignment> assignments = new ArrayList<PotentialAssignment>();
			assignments.add(PotentialAssignment.forValue(AbstractWebDriverTestCase.WebDriverKind.Chrome.toString(), AbstractWebDriverTestCase.WebDriverKind.Chrome));
			return assignments;
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@ParametersSuppliedBy(SingleWebDriverSupplier.class)
	public @interface SingleWebDriver {
	}
	public static class SingleWebDriverSupplier extends ParameterSupplier {

		@Override
		public List<PotentialAssignment> getValueSources(final ParameterSignature sig) {

			final List<PotentialAssignment> assignments = new ArrayList<PotentialAssignment>();
			assignments.add(PotentialAssignment.forValue(AbstractWebDriverTestCase.WebDriverKind.Chrome.toString(), AbstractWebDriverTestCase.WebDriverKind.Chrome));
			return assignments;
		}
	}

}
