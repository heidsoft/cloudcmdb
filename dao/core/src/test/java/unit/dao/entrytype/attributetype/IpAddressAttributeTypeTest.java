package unit.dao.entrytype.attributetype;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType.Type;
import org.junit.Before;
import org.junit.Test;

public class IpAddressAttributeTypeTest {

	private IpAddressAttributeType typeIpv4;
	private IpAddressAttributeType typeIpv6;

	@Before
	public void setUp() throws Exception {
		typeIpv4 = new IpAddressAttributeType(Type.IPV4);
		typeIpv6 = new IpAddressAttributeType(Type.IPV6);
	}

	@Test
	public void nullEmptyOrBlankConvertedToNull() throws Exception {
		// given
		final String nullValue = null;
		final String emptyValue = EMPTY;
		final String blankValue = "\t ";

		// when
		final String convertedNullValue = typeIpv4.convertValue(nullValue);
		final String convertedEmptyValue = typeIpv4.convertValue(emptyValue);
		final String convertedBlankValue = typeIpv4.convertValue(blankValue);

		// then
		assertThat(convertedNullValue, is(nullValue()));
		assertThat(convertedEmptyValue, is(nullValue()));
		assertThat(convertedBlankValue, is(nullValue()));
	}

	@Test
	public void invalidIpv4AddressThrowsException() throws Exception {
		// given
		final Iterable<String> values = asList( //
				"foo.bar.baz.lol", //
				"192.168.1.foo", "192.168.foo.1", "192.foo.1.1", "foo.168.1.1", //
				"256.168.1.1", "192.256.1.1", "192.168.256.1", "192.168.1.256", //
				"192.168.1", "192.168", "192." //
		);

		// when
		for (final String value : values) {
			try {
				typeIpv4.convertValue(value);
				fail("should not be able to convert " + value);
			} catch (final Exception e) {
				// ok
			}
		}
	}

	@Test
	public void validIpv4Address() throws Exception {
		// given
		final Iterable<String> values = asList( //
				"0.0.0.0", "123.0.0.0", "234.0.0.0", "255.0.0.0", //
				"1.0.0.0", "1.123.0.0", "1.234.0.0", "1.255.0.0", //
				"1.2.0.0", "1.2.123.0", "1.2.234.0", "1.2.255.0", //
				"1.2.3.0", "1.2.3.123", "1.2.3.234", "1.2.3.255" //
		);

		// when
		for (final String value : values) {
			try {
				typeIpv4.convertValue(value);
			} catch (final Exception e) {
				fail("should be able to convert " + value);
			}
		}
	}

	@Test
	public void validIpv6Address() throws Exception {
		// given
		final Iterable<String> values = asList( //
				"1:2:3:4:5:6:7:8", //
				"1::", "1:2:3:4:5:6:7::", //
				"1::8", "1:2:3:4:5:6::8", "1:2:3:4:5:6::8", //
				"1::7:8", "1:2:3:4:5::7:8", "1:2:3:4:5::8",//
				"1::6:7:8", "1:2:3:4::6:7:8", "1:2:3:4::8", //
				"1::5:6:7:8", "1:2:3::5:6:7:8", "1:2:3::8", //
				"1::4:5:6:7:8", "1:2::4:5:6:7:8", "1:2::8", //
				"1::3:4:5:6:7:8", "1::3:4:5:6:7:8", "1::8", //
				"::2:3:4:5:6:7:8", "::2:3:4:5:6:7:8", "::8", "::", "::1/128", //
				"fe80::7:8%eth0", "fe80::7:8%1", //
				"::255.255.255.255", "::ffff:255.255.255.255", "::ffff:0:255.255.255.255", //
				"2001:db8:3:4::192.0.2.33", "64:ff9b::192.0.2.33", //
				"11:11:11:11:11:11:11:11", "11:11:11:11:11:11:11:11/64" //
		);

		// when
		for (final String value : values) {
			try {
				typeIpv6.convertValue(value);
			} catch (final Exception e) {
				fail("should be able to convert " + value);
			}
		}
	}

	@Test
	public void invalidClassForIpv4ThrowsException() throws Exception {
		// given
		final Iterable<String> values = asList("192.168.1.1/foo", "192.168.1.1/0", "192.168.1.1/1", "192.168.1.1/2",
				"192.168.1.1/3", "192.168.1.1/4", "192.168.1.1/5", "192.168.1.1/6", "192.168.1.1/7", "192.168.1.1/33");

		// when
		for (final String value : values) {
			try {
				typeIpv4.convertValue(value);
				fail("should not be able to convert " + value);
			} catch (final Exception e) {
				// ok
			}
		}
	}

	@Test(expected = RuntimeException.class)
	public void ipv6ForIpv4TypeThrowsException() throws Exception {
		// when
		typeIpv4.convertValue("1:2:3:4:5:6:7:8");
	}

	@Test(expected = RuntimeException.class)
	public void ipv6ForIpv6TypeThrowsException() throws Exception {
		// when
		typeIpv6.convertValue("192.168.1.1");
	}

	@Test(expected = RuntimeException.class)
	public void hostResolutionNotTried() throws Exception {
		// given
		final String hostname = "example.com";

		// when
		typeIpv4.convertValue(hostname);
	}

	@Test
	public void valueIsTrimmed() throws Exception {
		// given
		final String raw = "\t 192.168.1.1 \n";

		// when
		final String converted = typeIpv4.convertValue(raw);

		// then
		assertThat(converted, equalTo("192.168.1.1"));
	}

}
