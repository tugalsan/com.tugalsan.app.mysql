/**
 * 
 */
package com.jspmyadmin.app.server.common.logic;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.jspmyadmin.app.server.common.beans.CommonListBean;
import com.jspmyadmin.framework.connection.AbstractLogic;
import com.jspmyadmin.framework.connection.ApiConnection;
import com.jspmyadmin.framework.constants.Constants;
import com.jspmyadmin.framework.exception.EncodingException;
import com.jspmyadmin.framework.web.utils.Bean;

/**
 * @author Yugandhar Gangu
 * @created_at 2016/02/10
 *
 */
public class PluginLogic extends AbstractLogic {

	/**
	 * 
	 * @param bean
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws JSONException
	 * @throws EncodingException
	 * @throws Exception
	 */
	public void fillBean(Bean bean) throws SQLException, JSONException, EncodingException {

		CommonListBean pluginBean = null;
		List<String[]> pluginInfoList = null;
		String[] pluginInfo = null;
		int length = 0;
		ApiConnection apiConnection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		ResultSetMetaData resultSetMetaData = null;
		JSONObject jsonObject = null;
		String orderBy = "PLUGIN_NAME";
		String sort = " ASC";
		boolean type = false;
		try {
			pluginBean = (CommonListBean) bean;
			apiConnection = getConnection();
			if (!super.isEmpty(pluginBean.getToken())) {
				jsonObject = new JSONObject(encodeObj.decode(pluginBean.getToken()));
				if (jsonObject.has(Constants.NAME)) {
					orderBy = jsonObject.getString(Constants.NAME);
				}
				if (jsonObject.has(Constants.TYPE)) {
					type = jsonObject.getBoolean(Constants.TYPE);
				}
			}
			if (type) {
				sort = " DESC";
			}
			statement = apiConnection
					.getStmtSelect("SELECT * FROM information_schema.PLUGINS ORDER BY " + orderBy + sort);
			resultSet = statement.executeQuery();
			resultSetMetaData = resultSet.getMetaData();
			length = resultSetMetaData.getColumnCount();

			pluginInfo = new String[length];
			for (int i = 0; i < length; i++) {
				pluginInfo[i] = resultSetMetaData.getColumnName(i + 1);
			}
			pluginBean.setColumnInfo(pluginInfo);

			pluginInfo = new String[length];
			for (int i = 0; i < length; i++) {
				jsonObject = new JSONObject();
				jsonObject.put(Constants.NAME, resultSetMetaData.getColumnName(i + 1));
				if (orderBy.equalsIgnoreCase(resultSetMetaData.getColumnName(i + 1))) {
					jsonObject.put(Constants.TYPE, !type);
				} else {
					jsonObject.put(Constants.TYPE, false);
				}
				pluginInfo[i] = encodeObj.encode(jsonObject.toString());
			}
			pluginBean.setSortInfo(pluginInfo);

			pluginInfoList = new ArrayList<String[]>();
			while (resultSet.next()) {
				pluginInfo = new String[length];
				for (int i = 0; i < length; i++) {
					pluginInfo[i] = resultSet.getString(i + 1);
				}
				pluginInfoList.add(pluginInfo);
			}
			pluginBean.setType(Boolean.toString(type));
			pluginBean.setField(orderBy);
			pluginBean.setData_list(pluginInfoList);
		} finally {
			close(resultSet);
			close(statement);
			close(apiConnection);
		}
	}
}
