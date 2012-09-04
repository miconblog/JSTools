<!--[UI_Window_Header]-->
<header role="banner" class='ui-head'>
	<div class="top_lnb">
		<h1 class="tit">{%=title%}</h1>
		
		{%if(nav){%}
		<nav role="navigation">
			{%if(nav.left){ %} 
			<a href="#" class="btn_top btn_cncl _action_{%=nav.left.action%}({%=uid%})">{%=nav.left.title%}</a>
			{%}%}
			{%if(nav.right){ %} 
			<a href="#" class="btn_top _action_{%=nav.right.action%}({%=uid%})">{%=nav.right.title%}</a>
			{%}%}
		</nav>
		{%}%}
	</div>
</header>
<!--[/UI_Window_Header]-->
<!--[UI_Window_Content]-->
<div role="main" class="ui-content" style="background-color: #ccc"></div>
<!--[/UI_Window_Content]-->

<!--[UI_Window]-->
<div id='{%=id%}' class="ui-window">
	<div role="main" class="ui-content" style="background-color: #ccc"></div>
</div>
<!--[/UI_Window]-->

<!--[UI_View]-->
<div id='{%=id%}' class='ui-view'></div>
<!--[/UI_View]-->