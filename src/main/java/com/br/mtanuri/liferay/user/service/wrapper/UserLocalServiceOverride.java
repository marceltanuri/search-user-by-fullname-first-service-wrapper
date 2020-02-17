package com.br.mtanuri.liferay.user.service.wrapper;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceWrapper;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceWrapper;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author marceltanuri
 */
@Component(
	immediate = true,
	property = {
	},
	service = ServiceWrapper.class
)
public class UserLocalServiceOverride extends UserLocalServiceWrapper {

	public UserLocalServiceOverride() {
		super(null);
	}

	@Reference(unbind = "-")
	private void serviceSetter(UserLocalService userLocalService) {
		setWrappedService(userLocalService);
	}



	@Override
	public List<User> search(long companyId, String keywords, int status, LinkedHashMap<String, Object> params,
			int start, int end, OrderByComparator<User> obc) {
		if (_log.isDebugEnabled()) {
			_log.debug("Overriding UserLocalServiceOverride search");
		}
		return searchByFullName(companyId, keywords, status, params, start, end, obc);
	}

	public List<User> searchByFullName(long companyId, String keywords, int status,
			LinkedHashMap<String, Object> params, int start, int end, OrderByComparator<User> obc) {

		if (keywords.contains(" ")) {

			String firstName = keywords.split(" ")[0];
			String lastName = keywords.substring(keywords.indexOf(" ")).trim();

			ClassLoader cl = PortalClassLoaderUtil.getClassLoader();
			DynamicQuery query = DynamicQueryFactoryUtil.forClass(User.class, cl);
			query.add(RestrictionsFactoryUtil.ilike("firstName", firstName));
			query.add(RestrictionsFactoryUtil.ilike("lastName", lastName));

			List<User> _1stList = UserLocalServiceUtil.dynamicQuery(query);

			query = DynamicQueryFactoryUtil.forClass(User.class, cl);
			query.add(RestrictionsFactoryUtil.ilike("firstName", "%" + firstName + "%"));
			query.add(RestrictionsFactoryUtil.ilike("lastName", "%" + lastName + "%"));

			List<User> _2ndList = UserLocalServiceUtil.dynamicQuery(query);

			List<User> _3hdList = super.search(companyId, keywords, status, params, start, end, obc);

			List<User> filtered1stList = new ArrayList<User>(Collections.unmodifiableList(_1stList));
			List<User> filtered2ndList = new ArrayList<User>(Collections.unmodifiableList(_2ndList));
			List<User> filtered3hdList = new ArrayList<User>(Collections.unmodifiableList(_3hdList));

			filtered2ndList.removeAll(filtered1stList);
			filtered3hdList.removeAll(filtered2ndList);
			filtered3hdList.removeAll(filtered1stList);
			filtered1stList.addAll(filtered2ndList);
			filtered1stList.addAll(filtered3hdList);

			return filtered1stList;
		}
		return super.search(companyId, keywords, status, params, start, end, obc);

	}
	private static final Log _log = LogFactoryUtil.getLog(UserLocalServiceOverride.class);

}