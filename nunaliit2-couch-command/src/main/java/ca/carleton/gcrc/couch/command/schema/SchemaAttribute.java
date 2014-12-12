package ca.carleton.gcrc.couch.command.schema;

import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

public class SchemaAttribute {
	
	static public SchemaAttribute fromJson(JSONObject jsonAttr) throws Exception {
		String type = jsonAttr.getString("type");
		
		SchemaAttribute attribute = new SchemaAttribute(type);

		// id
		{
			String id = jsonAttr.optString("id",null);
			if( null != id ){
				attribute.setId(id);
			}
		}

		// label
		{
			String label = jsonAttr.optString("label",null);
			if( null != label ){
				attribute.setLabel(label);
			}
		}

		// includedInBrief
		{
			boolean includedInBrief = jsonAttr.optBoolean("includedInBrief",false);
			attribute.setIncludeInBrief(includedInBrief);
		}

		// excludedFromDisplay
		{
			boolean excludedFromDisplay = jsonAttr.optBoolean("excludedFromDisplay",false);
			attribute.setExcludedFromDisplay(excludedFromDisplay);
		}

		// excludedFromForm
		{
			boolean excludedFromForm = jsonAttr.optBoolean("excludedFromForm",false);
			attribute.setExcludedFromForm(excludedFromForm);
		}
		
		// options
		{
			JSONArray jsonOptions = jsonAttr.optJSONArray("options");
			if( null != jsonOptions ){
				for(int i=0,e=jsonOptions.length(); i<e; ++i){
					JSONObject jsonOption = jsonOptions.getJSONObject(i);
					SelectionOption option = SelectionOption.fromJson(jsonOption);
					attribute.addOption(option);
				}
			}
		}
		
		return attribute;
	}
	
	private String type;
	private String id;
	private String label;
	private boolean includedInBrief;
	private boolean excludedFromDisplay;
	private boolean excludedFromForm;
	private List<SelectionOption> options = new Vector<SelectionOption>();

	public SchemaAttribute(String type){
		this.type = type;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isIncludedInBrief() {
		return includedInBrief;
	}

	public void setIncludeInBrief(boolean includedInBrief) {
		this.includedInBrief = includedInBrief;
	}

	public boolean isExcludedFromDisplay() {
		return excludedFromDisplay;
	}

	public void setExcludedFromDisplay(boolean excludedFromDisplay) {
		this.excludedFromDisplay = excludedFromDisplay;
	}

	public boolean isExcludedFromForm() {
		return excludedFromForm;
	}

	public void setExcludedFromForm(boolean excludedFromForm) {
		this.excludedFromForm = excludedFromForm;
	}

	public List<SelectionOption> getOptions() {
		return options;
	}

	public void addOption(SelectionOption option) {
		this.options.add(option);
	}
	
	public SelectionOption getDefaultOption(){
		SelectionOption defOption = null;
		
		for(SelectionOption option : options){
			if( option.isDefault() ){
				defOption = option;
				break;
			}
		}
		
		return defOption;
	}

	public JSONObject toJson() throws Exception {
		JSONObject jsonAttr = new JSONObject();
		
		jsonAttr.put("type", type);
		
		if( null != id ) jsonAttr.put("id", id);
		if( null != label ) jsonAttr.put("label", label);
		if( includedInBrief ) jsonAttr.put("includedInBrief", true);
		if( excludedFromDisplay ) jsonAttr.put("excludedFromDisplay", true);
		if( excludedFromForm ) jsonAttr.put("excludedFromForm", true);
		
		if( options.size() > 0 ){
			JSONArray jsonOptions = new JSONArray();
			
			for(SelectionOption option : options){
				JSONObject jsonOption = option.toJson();
				jsonOptions.put(jsonOption);
			}
			
			jsonAttr.put("options",jsonOptions);
		}
		
		return jsonAttr;
	}

	public void addCreateField(JSONObject doc, JSONObject schemaDoc) throws Exception {
		if( "string".equals(type) ){
			if( null != id ){
				schemaDoc.put(id, "");
			}
			
		} else if( "textarea".equals(type) ){
			if( null != id ){
				schemaDoc.put(id, "");
			}
			
		} else if( "date".equals(type) ){
			// leave date attributes as undefined
			
		} else if( "reference".equals(type) ){
			// leave reference attributes as undefined
			
		} else if( "selection".equals(type) ){
			if( null != id ){
				SelectionOption defOption = getDefaultOption();
				if( null == defOption ){
					schemaDoc.put(id, "");
				} else {
					schemaDoc.put(id, defOption.getValue());
				}
			}

		} else {
			throw new Exception("Unable to include type "+type+" in create");
		}
	}

	public void printBrief(PrintWriter pw, String schemaName) throws Exception {
		if( includedInBrief ){
			if( "string".equals(type) ){
				if( null != id ){
					pw.print("{{#"+schemaName+"}}");
					pw.print("{{"+id+"}}");
					pw.print("{{/"+schemaName+"}}");
				}
				
			} else if( "textarea".equals(type) ){
				if( null != id ){
					pw.print("{{#"+schemaName+"}}");
					pw.print("{{"+id+"}}");
					pw.print("{{/"+schemaName+"}}");
				}
				
			} else if( "selection".equals(type) ){
				if( null != id ){
					pw.print("{{#"+schemaName+"}}");
					pw.print("{{"+id+"}}");
					pw.print("{{/"+schemaName+"}}");
				}
				
			} else if( "date".equals(type) ){
				if( null != id ){
					pw.print("{{#"+schemaName+"}}");
					pw.print("{{#"+id+"}}");
					pw.print("{{date}}");
					pw.print("{{/"+id+"}}");
					pw.print("{{/"+schemaName+"}}");
				}
				
			} else if( "reference".equals(type) ){
				if( null != id ){
					pw.print("{{#"+schemaName+"}}");
					pw.print("{{#"+id+"}}");
					pw.print("{{#doc}}");
					pw.print("<span class=\"n2s_briefDisplay\">{{.}}</span>");
					pw.print("{{/doc}}");
					pw.print("{{/"+id+"}}");
					pw.print("{{/"+schemaName+"}}");
				}
					
			} else {
				throw new Exception("Unable to include type "+type+" in brief");
			}
		}
	}

	public void printDisplay(PrintWriter pw, String schemaName) throws Exception {
		if( false == excludedFromDisplay ){
			String label = this.label;
			String labelLocalizeClass = " n2s_localize";
			if( null == label ){
				label = id;
				labelLocalizeClass = "";
			}

			if( "string".equals(type) 
			 || "textarea".equals(type)
			 || "selection".equals(type) ){
				if( null != id ){
					pw.println("{{#"+schemaName+"}}");
					pw.println("\t{{#"+id+"}}");

					pw.println("\t\t<div class=\""+schemaName+"_"+id+"\">");

					pw.println("\t\t\t<div class=\"label"+labelLocalizeClass+"\">"+label+"</div>");
					if( "textarea".equals(type) ){
						pw.println("\t\t\t<div class=\"value n2s_preserveSpaces n2s_installMaxHeight n2s_convertTextUrlToLink\" _maxheight=\"100\">{{.}}</div>");
					} else {
						pw.println("\t\t\t<div class=\"value\">{{.}}</div>");
					}
					pw.println("\t\t\t<div class=\"end\"></div>");
					
					pw.println("\t\t</div>");
					
					
					pw.println("\t{{/"+id+"}}");
					pw.println("{{/"+schemaName+"}}");
				}
				
			} else if( "date".equals(type) ){
				if( null != id ){
					pw.println("{{#"+schemaName+"}}");
					pw.println("\t{{#"+id+"}}");

					pw.println("\t\t<div class=\""+schemaName+"_"+id+"\">");

					pw.println("\t\t\t<div class=\"label"+labelLocalizeClass+"\">"+label+"</div>");
					pw.println("\t\t\t<div class=\"value\">{{date}}</div>");
					pw.println("\t\t\t<div class=\"end\"></div>");
					
					pw.println("\t\t</div>");
					
					
					pw.println("\t{{/"+id+"}}");
					pw.println("{{/"+schemaName+"}}");
				}
				
			} else if( "reference".equals(type) ){
				if( null != id ){
					pw.println("{{#"+schemaName+"}}");
					pw.println("\t{{#"+id+"}}");

					pw.println("\t\t<div class=\""+schemaName+"_"+id+"\">");

					pw.println("\t\t\t<div class=\"label"+labelLocalizeClass+"\">"+label+"</div>");
					pw.println("\t\t\t<div class=\"value\"><a href=\"#\" class=\"n2s_referenceLink\">{{doc}}</a></div>");
					pw.println("\t\t\t<div class=\"end\"></div>");
					
					pw.println("\t\t</div>");
					
					
					pw.println("\t{{/"+id+"}}");
					pw.println("{{/"+schemaName+"}}");
				}
							
			} else {
				throw new Exception("Unable to include type "+type+" in display");
			}
		}
	}

	public void printForm(PrintWriter pw, String schemaName) throws Exception {
		if( false == excludedFromForm ){
			String label = this.label;
			String labelLocalizeClass = " n2s_localize";
			if( null == label ){
				label = id;
				labelLocalizeClass = "";
			}

			if( "string".equals(type) 
			 || "textarea".equals(type) 
			 || "reference".equals(type) 
			 || "date".equals(type) ){
				if( null != id ){
					String fieldType = "";
					if( "textarea".equals(type) ){
						fieldType = ",textarea";
					} else if( "date".equals(type) ){
						fieldType = ",date";
					} else if( "reference".equals(type) ){
						fieldType = ",reference";
					}
					
					pw.println("{{#"+schemaName+"}}");

					pw.println("\t<div class=\""+schemaName+"_"+id+"\">");

					pw.println("\t\t<div class=\"label"+labelLocalizeClass+"\">"+label+"</div>");
					pw.println("\t\t<div class=\"value\">{{#:field}}"+id+fieldType+"{{/:field}}</div>");
					pw.println("\t\t<div class=\"end\"></div>");
					
					pw.println("\t</div>");
					
					
					pw.println("{{/"+schemaName+"}}");
				}
				
			} else if( "selection".equals(type) ){
				if( null != id ){
					pw.println("{{#"+schemaName+"}}");

					pw.println("\t<div class=\""+schemaName+"_"+id+"\">");

					pw.println("\t\t<div class=\"label"+labelLocalizeClass+"\">"+label+"</div>");
					pw.println("\t\t<div class=\"value\">");
					pw.println("\t\t\t<select class=\"{{#:input}}"+id+"{{/:input}}\">");
					
					for(SelectionOption option : options){
						pw.print("\t\t\t\t<option value=\""+option.getValue()+"\">");
						String optLabel = option.getLabel();
						if( null == optLabel ){
							optLabel = option.getValue();
						}
						pw.print(optLabel);
						pw.println("</option>");
					}
					
					pw.println("\t\t\t</select>");
					pw.println("\t\t</div>");
					pw.println("\t\t<div class=\"end\"></div>");
					
					pw.println("\t</div>");
					
					
					pw.println("{{/"+schemaName+"}}");
				}
						
			} else {
				throw new Exception("Unable to include type "+type+" in form");
			}
		}
	}
}