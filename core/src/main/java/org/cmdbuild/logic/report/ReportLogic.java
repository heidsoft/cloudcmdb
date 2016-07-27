package org.cmdbuild.logic.report;

import java.util.Map;

import javax.activation.DataHandler;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.logic.Logic;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Optional;

public interface ReportLogic extends Logic {

	Marker marker = MarkerFactory.getMarker(ReportLogic.class.getName());

	interface Report {

		int getId();

		String getTitle();

		String getType();

		String getDescription();

	}

	interface Extension {

		void accept(ExtensionVisitor visitor);

	}

	interface ExtensionVisitor {

		void visit(Csv extension);

		void visit(Odt extension);

		void visit(Pdf extension);

		void visit(Rtf extension);

		void visit(Zip extension);

	}

	class Csv implements Extension {

		private Csv() {
			// use flyweight object
		}

		@Override
		public void accept(final ExtensionVisitor visitor) {
			visitor.visit(this);
		}

	}

	class Odt implements Extension {

		private Odt() {
			// use flyweight object
		}

		@Override
		public void accept(final ExtensionVisitor visitor) {
			visitor.visit(this);
		}

	}

	class Pdf implements Extension {

		private Pdf() {
			// use flyweight object
		}

		@Override
		public void accept(final ExtensionVisitor visitor) {
			visitor.visit(this);
		}

	}

	class Rtf implements Extension {

		private Rtf() {
			// use flyweight object
		}

		@Override
		public void accept(final ExtensionVisitor visitor) {
			visitor.visit(this);
		}

	}

	class Zip implements Extension {

		private Zip() {
			// use flyweight object
		}

		@Override
		public void accept(final ExtensionVisitor visitor) {
			visitor.visit(this);
		}

	}

	class Extensions {

		private static Extension csv = new Csv();
		private static Extension odt = new Odt();
		private static Extension pdf = new Pdf();
		private static Extension rtf = new Rtf();
		private static Extension zip = new Zip();

		public static Extension csv() {
			return csv;
		}

		public static Extension odt() {
			return odt;
		}

		public static Extension pdf() {
			return pdf;
		}

		public static Extension rtf() {
			return rtf;
		}

		public static Extension zip() {
			return zip;
		}

	}

	Iterable<Report> readAll();

	Optional<Report> read(int reportId);

	Iterable<CMAttribute> parameters(int id);

	DataHandler download(int reportId, Extension extension, Map<String, ? extends Object> parameters);

}
