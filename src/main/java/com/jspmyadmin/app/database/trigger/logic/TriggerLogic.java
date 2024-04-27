/**
 * 
 */
package com.jspmyadmin.app.database.trigger.logic;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.jspmyadmin.app.database.trigger.beans.TriggerBean;
import com.jspmyadmin.app.database.trigger.beans.TriggerInfo;
import com.jspmyadmin.app.database.trigger.beans.TriggerListBean;
import com.jspmyadmin.framework.connection.AbstractLogic;
import com.jspmyadmin.framework.connection.ApiConnection;
import com.jspmyadmin.framework.constants.Constants;
import com.jspmyadmin.framework.web.utils.Bean;

/**
 * @author Yugandhar Gangu
 * @created_at 2016/03/28
 *
 */
public class TriggerLogic extends AbstractLogic {

	/**
	 * 
	 * @param bean
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public void fillListBean(Bean bean) throws SQLException {

		TriggerListBean triggerListBean = null;

		ApiConnection apiConnection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;

		List<TriggerInfo> triggerInfoList = null;
		TriggerInfo triggerInfo = null;
		int total = 0;
		String trigger = "Trigger";
		String event = "Event";
		String table = "Table";
		String timing = "Timing";
		String definer = "Definer";
		try {
			triggerListBean = (TriggerListBean) bean;
			apiConnection = getConnection(bean.getRequest_db());
			statement = apiConnection.getStmtSelect("SHOW TRIGGERS");
			resultSet = statement.executeQuery();
			triggerInfoList = new ArrayList<TriggerInfo>();
			while (resultSet.next()) {
				triggerInfo = new TriggerInfo();
				triggerInfo.setTrigger_name(resultSet.getString(trigger));
				triggerInfo.setEvent_type(resultSet.getString(event));
				triggerInfo.setTable_name(resultSet.getString(table));
				triggerInfo.setEvent_time(resultSet.getString(timing));
				triggerInfo.setDefiner(resultSet.getString(definer));
				triggerInfoList.add(triggerInfo);
				total++;
			}
			triggerListBean.setTrigger_list(triggerInfoList);
			triggerListBean.setTotal(Integer.toString(total));
		} finally {
			close(resultSet);
			close(statement);
			close(apiConnection);
		}
	}

	/**
	 * 
	 * @param name
	 * @param database
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public boolean isExisted(String name, String database) throws SQLException {

		ApiConnection apiConnection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;

		StringBuilder builder = null;
		try {
			apiConnection = getConnection(database);
			builder = new StringBuilder();
			builder.append("SELECT trigger_name FROM information_schema.triggers");
			builder.append(" WHERE trigger_schema = ? AND trigger_name = ?");
			statement = apiConnection.getStmtSelect(builder.toString());
			statement.setString(1, database);
			statement.setString(2, name);
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				return true;
			}
		} finally {
			close(resultSet);
			close(statement);
			close(apiConnection);
		}
		return false;
	}

	/**
	 * 
	 * @param bean
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws JSONException
	 */
	public String showCreate(Bean bean) throws SQLException, JSONException {

		String result = null;

		ApiConnection apiConnection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;

		TriggerListBean triggerListBean = null;
		StringBuilder builder = null;
		JSONObject jsonObject = null;
		try {
			triggerListBean = (TriggerListBean) bean;
			if (triggerListBean.getTriggers() != null) {
				apiConnection = getConnection(bean.getRequest_db());
				String temp = "SHOW CREATE TRIGGER `";
				builder = new StringBuilder();
				jsonObject = new JSONObject();
				for (int i = 0; i < triggerListBean.getTriggers().length; i++) {
					builder.append(temp);
					builder.append(triggerListBean.getTriggers()[i]);
					builder.append(Constants.SYMBOL_TEN);
					statement = apiConnection.getStmtSelect(builder.toString());
					resultSet = statement.executeQuery();
					if (resultSet.next()) {
						jsonObject.append(resultSet.getString(1), resultSet.getString(3));
					}
					builder.delete(0, builder.length());
				}
				result = jsonObject.toString();
			}
		} finally {
			close(resultSet);
			close(statement);
			close(apiConnection);
		}
		return result;
	}

	/**
	 * 
	 * @param bean
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public void drop(Bean bean) throws SQLException {

		ApiConnection apiConnection = null;
		PreparedStatement statement = null;

		TriggerListBean triggerListBean = null;
		StringBuilder builder = null;
		try {
			triggerListBean = (TriggerListBean) bean;
			if (triggerListBean.getTriggers() != null) {
				apiConnection = super.getConnection(bean.getRequest_db());
				String temp = "DROP TRIGGER IF EXISTS `";
				builder = new StringBuilder();
				for (int i = 0; i < triggerListBean.getTriggers().length; i++) {
					builder.append(temp);
					builder.append(triggerListBean.getTriggers()[i]);
					builder.append(Constants.SYMBOL_TEN);
					statement = apiConnection.getStmt(builder.toString());
					statement.execute();
					builder.delete(0, builder.length());
				}
			}
		} finally {
			close(statement);
			close(apiConnection);
		}
	}

	/**
	 * 
	 * @param database
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public List<String> getTriggerList(String database) throws SQLException {
		List<String> triggerList = new ArrayList<String>();

		ApiConnection apiConnection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;

		String trigger = "Trigger";
		try {
			apiConnection = getConnection(database);
			statement = apiConnection.getStmtSelect("SHOW TRIGGERS");
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				triggerList.add(resultSet.getString(trigger));
			}
		} finally {
			close(resultSet);
			close(statement);
			close(apiConnection);
		}
		return triggerList;
	}

	/**
	 * 
	 * @param bean
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public String save(Bean bean) throws SQLException {

		String result = null;

		ApiConnection apiConnection = null;
		PreparedStatement statement = null;

		TriggerBean triggerBean = null;
		StringBuilder builder = null;
		String[] temp = null;
		try {
			triggerBean = (TriggerBean) bean;

			builder = new StringBuilder();
			builder.append("CREATE ");
			if (!isEmpty(triggerBean.getDefiner())) {
				if (Constants.CURRENT_USER.equalsIgnoreCase(triggerBean.getDefiner())) {
					builder.append("DEFINER = ");
					builder.append(triggerBean.getDefiner());
					builder.append(Constants.SPACE);
				} else if (!isEmpty(triggerBean.getDefiner_name())) {
					temp = triggerBean.getDefiner_name().split(Constants.SYMBOL_AT);
					builder.append("DEFINER = ");
					if (temp.length < 2) {
						builder.append(Constants.SYMBOL_TEN);
						builder.append(temp[0]);
						builder.append(Constants.SYMBOL_TEN);
						builder.append(Constants.SPACE);
					} else {
						builder.append(Constants.SYMBOL_TEN);
						builder.append(temp[0]);
						builder.append(Constants.SYMBOL_TEN);
						builder.append(Constants.SYMBOL_AT);
						builder.append(Constants.SYMBOL_TEN);
						builder.append(temp[1]);
						if (!temp[1].endsWith(Constants.SYMBOL_TEN)) {
							builder.append(Constants.SYMBOL_TEN);
						}
						builder.append(Constants.SPACE);
					}
				}
			}
			builder.append("TRIGGER ");
			builder.append(Constants.SYMBOL_TEN);
			builder.append(triggerBean.getTrigger_name());
			builder.append(Constants.SYMBOL_TEN);
			builder.append(Constants.SPACE);

			builder.append(triggerBean.getTrigger_time());
			builder.append(Constants.SPACE);
			builder.append(triggerBean.getTrigger_event());

			builder.append(" ON ");
			builder.append(Constants.SYMBOL_TEN);
			builder.append(triggerBean.getDatabase_name());
			builder.append(Constants.SYMBOL_TEN);
			builder.append(Constants.SYMBOL_DOT);
			builder.append(Constants.SYMBOL_TEN);
			builder.append(triggerBean.getTable_name());
			builder.append(Constants.SYMBOL_TEN);

			builder.append(" FOR EACH ROW ");

			if (!isEmpty(triggerBean.getTrigger_order()) && !isEmpty(triggerBean.getOther_trigger_name())) {
				builder.append(triggerBean.getTrigger_order());
				builder.append(Constants.SPACE);
				builder.append(triggerBean.getOther_trigger_name());
				builder.append(Constants.SPACE);
			}
			builder.append(triggerBean.getTrigger_body());
			if (Constants.YES.equalsIgnoreCase(triggerBean.getAction())) {
				apiConnection = getConnection(bean.getRequest_db());
				statement = apiConnection.getStmt(builder.toString());
				statement.execute();
			} else {
				result = builder.toString();
			}
		} finally {
			close(statement);
			close(apiConnection);
		}
		return result;
	}
}
