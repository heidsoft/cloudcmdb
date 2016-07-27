package org.cmdbuild.service.rest.v1.model.adapter;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.service.rest.v1.model.Models.newValues;

import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.ObjectUtils;
import org.cmdbuild.service.rest.v1.model.Model;
import org.cmdbuild.service.rest.v1.model.Values;

public abstract class ModelToValuesAdapter<T extends Model> extends XmlAdapter<Values, T> {

	protected static <T> T getAndRemove(final Map<String, Object> mapType, final String key, final Class<T> type) {
		for (final Entry<String, Object> element : mapType.entrySet()) {
			if (ObjectUtils.equals(element.getKey(), key)) {
				final Object value = element.getValue();
				mapType.remove(key);
				final Object _value;
				if (Long.class.equals(type)) {
					String s;
					if (value instanceof Long) {
						s = Long.class.cast(value).toString();
					} else if (value instanceof Integer) {
						s = Integer.class.cast(value).toString();
					} else {
						s = value.toString();
					}
					s = String.class.cast(s);
					_value = isBlank(s) ? null : Long.parseLong(s);
				} else {
					_value = value;
				}
				return type.cast(_value);
			}
		}
		return null;
	}

	@Override
	public final Values marshal(final T v) throws Exception {
		return newValues() //
				.withValues(modelToValues(v)) //
				.build();
	}

	protected abstract Values modelToValues(T input);

	@Override
	public final T unmarshal(final Values v) throws Exception {
		return valuesToModel(v);
	}

	protected abstract T valuesToModel(Values input);

}