package com.liferay.notification.web.internal.foo;

import com.liferay.frontend.taglib.servlet.taglib.ScreenNavigationCategory;
import com.liferay.frontend.taglib.servlet.taglib.ScreenNavigationEntry;
import com.liferay.frontend.taglib.servlet.taglib.util.JSPRenderer;
import com.liferay.object.model.ObjectLayoutTab;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

@Component(
	property = {
		"screen.navigation.category.order:Integer=30",
		"screen.navigation.entry.order:Integer=30"
	},
	service = {ScreenNavigationCategory.class, ScreenNavigationEntry.class}
)
public class FooTab3ScreenNavigationCategory
	implements ScreenNavigationCategory,
	ScreenNavigationEntry<ObjectLayoutTab> {

	@Override
	public String getCategoryKey() {
		return "categoryKey3";
	}

	@Override
	public String getEntryKey() {
		return "entryKey3";
	}

	@Override
	public String getLabel(Locale locale) {
		return "Client's Tab 3";
	}

	@Override
	public String getScreenNavigationKey() {
		return "33478c0d-a020-6040-8876-67d7989d17d0";
	}

	@Override
	public void render(
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse) throws IOException {

		jspRenderer.renderJSP(
			servletContext, httpServletRequest, httpServletResponse,
			"/foo/foo.jsp");

	}

	@Reference
	protected JSPRenderer jspRenderer;

	@Reference(target = "(osgi.web.symbolicname=com.liferay.notification.web)")
	protected ServletContext servletContext;
}