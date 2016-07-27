package org.cmdbuild.common.java.sql;

public class DataSourceTypes {

	public static interface DataSourceType {

		void accept(DataSourceTypeVisitor visitor);

	}

	public static class MySql implements DataSourceType {

		private MySql() {
			// use factory method
		}

		@Override
		public void accept(final DataSourceTypeVisitor visitor) {
			visitor.visit(this);
		}

	}

	public static class Oracle implements DataSourceType {

		private Oracle() {
			// use factory method
		}

		@Override
		public void accept(final DataSourceTypeVisitor visitor) {
			visitor.visit(this);
		}

	}

	public static class PostgreSql implements DataSourceType {

		private PostgreSql() {
			// use factory method
		}

		@Override
		public void accept(final DataSourceTypeVisitor visitor) {
			visitor.visit(this);
		}

	}

	public static class SqlServer implements DataSourceType {

		private SqlServer() {
			// use factory method
		}

		@Override
		public void accept(final DataSourceTypeVisitor visitor) {
			visitor.visit(this);
		}

	}

	public static interface DataSourceTypeVisitor {

		void visit(MySql type);

		void visit(Oracle type);

		void visit(PostgreSql type);

		void visit(SqlServer type);

	}

	private static MySql mysql = new MySql();
	private static Oracle oracle = new Oracle();
	private static PostgreSql postgresql = new PostgreSql();
	private static SqlServer sqlserver = new SqlServer();

	public static MySql mysql() {
		return mysql;
	}

	public static Oracle oracle() {
		return oracle;
	}

	public static PostgreSql postgresql() {
		return postgresql;
	}

	public static SqlServer sqlserver() {
		return sqlserver;
	}

	private DataSourceTypes() {
		// prevents instantiation
	}

}
