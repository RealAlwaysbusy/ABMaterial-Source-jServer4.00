package com.ab.abmaterial;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Author;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4j.object.WebSocket.SimpleFuture;

@Author("Alain Bailleul")  
@ShortName("ABMSlider")
@Events(values={"Changed(value as String)"})
public class ABMSlider extends ABMComponent {
	
	private static final long serialVersionUID = -816058329874325133L;
	protected ThemeSlider Theme=new ThemeSlider();
	protected double mValue=0;
	public String Connect=ABMaterial.SLIDER_CONNECT_LOWER;
	protected double mStep=0;
	protected double mMin=0;
	protected double mMax;
	protected String Direction=ABMaterial.SLIDER_DIRECTION_LEFTTORIGHT;
	public String HandleToolTip="";
	public boolean Animate=true;
	protected boolean IsDirtyValue=false;
	public int VerticalHeightPx=200;
	
	public String PaddingLeft=""; //"0.75rem";
	public String PaddingRight=""; //"0.75rem";
	public String PaddingTop=""; //"0rem";
	public String PaddingBottom=""; //"0rem";
	public String MarginTop=""; //"0px";
	public String MarginBottom=""; //"0px";
	public String MarginLeft=""; //"0px";
	public String MarginRight=""; //"0px";
	
	protected boolean GotLastValue=true;
		
	public void Initialize(ABMPage page, String id, double value, double min, double max, double step, String themeName) {
		this.ID = id;			
		this.Page = page;
		this.Type = ABMaterial.UITYPE_SLIDER;	
		this.mValue = value;
		this.mMin = min;
		this.mMax = max;
		this.mStep = step;
		if (!themeName.equals("")) {
			if (Page.CompleteTheme.Sliders.containsKey(themeName.toLowerCase())) {
				Theme = Page.CompleteTheme.Sliders.get(themeName.toLowerCase()).Clone();				
			}
		}	
		IsInitialized=true;
		IsDirtyValue=false;
		IsVisibilityDirty=false;
	}
	
	@Override
	protected void ResetTheme() {
		UseTheme(Theme.ThemeName);
	}
	
	@Override
	protected String RootID() {
		return ArrayName.toLowerCase() + ID.toLowerCase();
	}
	
	public void SetValue(double value) {
		mValue=value;
		IsDirtyValue=true;
	}
	
	
	public double GetValue() {
		if (GotLastValue) {
			return mValue;
		}
		GotLastValue=true;
		if (!IsDirtyValue) {
			StringBuilder s = new StringBuilder();
			String tmpVar = ParentString + ArrayName.toLowerCase() + ID.toLowerCase();
			tmpVar = tmpVar.replace("-", "_");
			s.append("var " + tmpVar + " = document.getElementById('" + ParentString + ArrayName.toLowerCase() + ID.toLowerCase() + "');");
			s.append("return " + tmpVar + ".noUiSlider.get();");
			BA.Log(s.toString());
			SimpleFuture j = Page.ws.EvalWithResult(s.toString(), null);
			String v = "";
			try {
				v = (String) j.getValue();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (v==null) {
				return 0;
			} else {
				mValue=Double.parseDouble(v);
				return mValue;
			}
		}
		return mValue;
	}
	
		
	@Override
	protected void AddArrayName(String arrayName) {	
		this.ArrayName += arrayName;
	}	
		
	public void UseTheme(String themeName) {
		if (!themeName.equals("")) {
			if (Page.CompleteTheme.Sliders.containsKey(themeName.toLowerCase())) {
				Theme = Page.CompleteTheme.Sliders.get(themeName.toLowerCase()).Clone();				
			}
		}
	}		
		
	@Override
	protected void CleanUp() {
		super.CleanUp();
	}
	
	@Override
	protected void RemoveMe() {
		ABMaterial.RemoveHTML(Page, ParentString + ArrayName.toLowerCase() + ID.toLowerCase());
	}
	
	@Override
	protected void FirstRun() {
		Page.ws.Eval(BuildJavaScript(), null);
		
		super.FirstRun();
	}
	
	protected String BuildJavaScript() {
		StringBuilder s = new StringBuilder();	
		String tmpVar = ParentString + ArrayName.toLowerCase() + ID.toLowerCase();
		tmpVar = tmpVar.replace("-", "_");
		s.append("var " + tmpVar + " = document.getElementById('" + ParentString + ArrayName.toLowerCase() + ID.toLowerCase() + "');");
		s.append("noUiSlider.create(" + tmpVar + ", {");
		s.append("start: [" + mValue + "],");
		switch (Connect) {
		case ABMaterial.SLIDER_CONNECT_NONE:
			s.append("connect: false,");	
			break;
		case ABMaterial.SLIDER_CONNECT_LOWER:
			s.append("connect: 'lower',");	
			break;
		case ABMaterial.SLIDER_CONNECT_UPPER:
			s.append("connect: 'upper',");	
			break;
		}
		if (mStep!=0) {
			s.append("step: " + mStep + ",");
		}
		
		s.append("direction: '" + Direction + "',");
		
		if (!Animate) {
			s.append("animate: false,");
		}
		s.append("range: {'min': [" + mMin + "], 'max': [" + mMax + "]},");
		s.append("theme: " + "'abmsl" + Theme.ThemeName.toLowerCase() + "'");
		if (!HandleToolTip.equals("")) {
			s.append(", format: " + HandleToolTip);  
		}
		s.append("});");
		
		s.append(tmpVar + ".noUiSlider.on('change', function( values, handle ){");
		
		if (!mB4JSUniqueKey.equals("")) {
			s.append("var codeToExecute = 'return _b4jsclasses[\"" + mB4JSUniqueKey + "\"].' + _b4jsvars['" + mB4JSUniqueKey + "_B4JSOnChange'];");
			s.append("codeToExecute = codeToExecute.replace(/B4JS#!#SVALUE/g, values[0]);");
			s.append("var tmpFunc = new Function(codeToExecute);");
			s.append("var fret=false;");
			s.append("try { ");
			s.append("fret = tmpFunc()");	
			s.append("} catch(err) {}");
			
			s.append("if (fret==true) {return;}");
		}
		
		s.append("b4j_raiseEvent('page_parseevent', {'eventname':'" + ArrayName.toLowerCase() + ID.toLowerCase() + "_changed','eventparams':'value', 'value':values[handle]});");
		s.append("});");		
		return s.toString();
	}
	
	@Override
	public void Refresh() {	
		RefreshInternal(true);
	}
		
	@Override
	protected void RefreshInternal(boolean DoFlush) {
		// TODO ExtraClasses not working
		super.RefreshInternal(DoFlush);
		getVisibility();
		GetValue();
		
		ABMaterial.ChangeVisibility(Page, ParentString + ArrayName.toLowerCase() + ID.toLowerCase(), mVisibility);
		if (mIsPrintableClass.equals("")) {
			ABMaterial.RemoveClass(Page, ParentString + ArrayName.toLowerCase() + ID.toLowerCase(), "no-print");
		} else {
			ABMaterial.AddClass(Page, ParentString + ArrayName.toLowerCase() + ID.toLowerCase(), "no-print");
		}
		if (mIsOnlyForPrintClass.equals("")) {
			ABMaterial.RemoveClass(Page, ParentString + ArrayName.toLowerCase() + ID.toLowerCase(), "only-print");
		} else {
			ABMaterial.AddClass(Page, ParentString + ArrayName.toLowerCase() + ID.toLowerCase(), "only-print");
		}
		
		String B4JSData1="";
		if (!mB4JSUniqueKey.equals("")) {
			B4JSData1 = mB4JSUniqueKey ;
		} else {
			B4JSData1 = "";
		}
		String tmpVar = ParentString + ArrayName.toLowerCase() + ID.toLowerCase();
		tmpVar = tmpVar.replace("-", "_");
		String B4JSData2=tmpVar;
		ABMaterial.SetProperty(Page, ParentString + ArrayName.toLowerCase() + ID.toLowerCase(), "data-b4js", B4JSData1);
		ABMaterial.SetProperty(Page, ParentString + ArrayName.toLowerCase() + ID.toLowerCase(), "data-b4jsextra", B4JSData2);
		
		if (IsDirtyValue) {
			
			StringBuilder s = new StringBuilder();	
			
			s.append("var " + tmpVar + " = document.getElementById('" + ParentString + ArrayName.toLowerCase() + ID.toLowerCase() + "');");
			s.append(tmpVar + ".noUiSlider.set(" + mValue + ");");
			Page.ws.Eval(s.toString(), null);
			if (DoFlush) {
				try {
					if (Page.ws.getOpen()) {
						if (Page.ShowDebugFlush) {BA.Log("Slider Refresh: " + ID);}
						Page.ws.Flush();Page.RunFlushed();
					}
				} catch (IOException e) {			
					//e.printStackTrace();
				}
			}
		}
		IsDirtyValue=false;
		
	}
	
	@Override
	protected String Build() {
		if (Theme.ThemeName.equals("default")) {
			Theme.Colorize(Page.CompleteTheme.MainColor);
		}
		StringBuilder s = new StringBuilder();	
		
		String tmpVar = ParentString + ArrayName.toLowerCase() + ID.toLowerCase();
		tmpVar = tmpVar.replace("-", "_");
		
		String B4JSData="";
		if (!mB4JSUniqueKey.equals("")) {
			B4JSData = " data-b4js=\"" + mB4JSUniqueKey + "\" data-b4jsextra=\"" + tmpVar + "\" ";
		} else {
			B4JSData = " data-b4js=\"\" data-b4jsextra=\"\" ";
		}
		
		s.append("<div id=\"" + ParentString + ArrayName.toLowerCase() + ID.toLowerCase() + "\" " + B4JSData + " style=\"" + BuildStyle() + "\" class=\"" + mVisibility + " " + mIsPrintableClass + mIsOnlyForPrintClass + super.BuildExtraClasses() + "\">");
		
		s.append(BuildBody());
		s.append("</div>");
		IsBuild=true;
		return s.toString();
	}
	
	@Hide
	protected String BuildStyle() {			
		StringBuilder s = new StringBuilder();
		
		if (MarginTop!="") s.append(" margin-top: " + MarginTop + ";");
		if (MarginBottom!="") s.append(" margin-bottom: " + MarginBottom + ";");
		if (MarginLeft!="") s.append(" margin-left: " + MarginLeft + ";");
		if (MarginRight!="") s.append(" margin-right: " + MarginRight + ";");
		if (PaddingTop!="") s.append(" padding-top: " + PaddingTop + ";");
		if (PaddingBottom!="") s.append(" padding-bottom: " + PaddingBottom + ";");
		if (PaddingLeft!="") s.append(" padding-left: " + PaddingLeft + ";");
		if (PaddingRight!="") s.append(" padding-right: " + PaddingRight + ";");
		
		
		s.append(" width: 100%");
		
		return s.toString();
	}
	
	@Hide
	protected String BuildClass() {			
		StringBuilder s = new StringBuilder();			
		return s.toString(); 
	}
	
	@Hide
	protected String BuildBody() {
		StringBuilder s = new StringBuilder();
		return s.toString();
	}
	
	@Override
	public String getVisibility() {
		if (GotLastVisibility) {
			return mVisibility;
		}
		GotLastVisibility=true;
		if (!mB4JSUniqueKey.equals("") && !IsVisibilityDirty) {
			if (JQ!=null) {
				anywheresoftware.b4a.objects.collections.List par = new anywheresoftware.b4a.objects.collections.List();
				par.Initialize();
				par.Add(ParentString + RootID());
				FutureVisibility = JQ.RunMethodWithResult("GetVisibility", par);				
			} else {
				FutureVisibility=null;				
			}
			if (!(FutureVisibility==null)) {
				try {
					this.mVisibility = ((String)FutureVisibility.getValue());					
				} catch (InterruptedException e) {					
					e.printStackTrace();
				} catch (TimeoutException e) {					
					e.printStackTrace();
				} catch (ExecutionException e) {					
					e.printStackTrace();
				} catch (IOException e) {					
					e.printStackTrace();
				}
			} else {
				//BA.Log("FutureText = null");
				
			}	
			return mVisibility;
		}
		
		return mVisibility;		
	}
		
	/**
	 * ONLY TO USE IN A B4JS CLASS!
	 */
	public void setB4JSVisibility(String visibility) {
		
	}
	
	/**
	 * ONLY TO USE IN A B4JS CLASS!
	 */
	public String getB4JSVisibility() {
		return "B4JS Dummy";
	}
		
	/**
	 * ONLY TO USE IN A B4JS CLASS!
	 */
	public void B4JSSetValue(double value) {
		
	}
	
	/**
	 * ONLY TO USE IN A B4JS CLASS!
	 */
	public double B4JSGetValue() {
		return 0;
	}
	
	
	/**
	 * Needs at least the params ABM.B4JS_PARAM_RANGESTART and ABM.B4JS_PARAM_RANGESTOP
	 * 
	 * every method you want to call with a B4JSOn... call MUST return a boolean
	 * returning true will consume the event in the browser and not call the B4J event (if any)
	 *
	 * e.g. myButton.B4JSOnClicked("MyJavascript", "AddToLabel", Array As Object(myCounter))
	 * if AddToLabel return true, then myButton_Clicked() will not be raised.
	 * if AddToLabel returns false, then myButton_Clicked() will be raised AFTER the B4JS method is done.
	 * 
	 * public Sub AddToLabel(MyCounter As Int) As Boolean
	 *      if myCounter mod 2 = 0 then	       
	 *		   Return True
	 *      else
	 *         Return False
	 *      end if
	 * End Sub
	 */
	public void B4JSOnChange(String B4JSClassName, String B4JSMethod, anywheresoftware.b4a.objects.collections.List Params) {
		PrepareEvent("B4JSOnChange", B4JSClassName, B4JSMethod, Params);		
	}
	
	@Override
	protected ABMComponent Clone() {
		ABMSlider c = new ABMSlider();
		c.ArrayName = ArrayName;
		c.CellId = CellId;
		c.ID = ID;
		c.Page = Page;
		c.RowId= RowId;
		c.Theme = Theme.Clone();
		c.Type = Type;
		c.mVisibility = mVisibility;		
		return c;
	}
		

}
