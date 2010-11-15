package net.chronakis.struts.tiles.example;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.chronakis.struts.tiles.DynamicTilesUtils;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class SetLayoutAction extends Action {

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String newLayout = request.getParameter("masterLayoutPage");
		if(newLayout != null)
			DynamicTilesUtils.setVariable("masterLayoutPage", newLayout, mapping.getModuleConfig().getPrefix(), request.getSession());

		String newLoggedinLayout = request.getParameter("loggedinBodyLayoutPage");
		if(newLoggedinLayout != null)
			DynamicTilesUtils.setVariable("loggedinBodyLayoutPage", newLoggedinLayout, mapping.getModuleConfig().getPrefix(), request.getSession());
		
		String referer = request.getHeader("referer");
		if(referer == null)
			return mapping.getInputForward();
			
		response.sendRedirect(referer);
		
		return null;
	}
}
