package br.ueg.openodonto.visao.converter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.richfaces.component.UIOrderingList;

import br.ueg.openodonto.controle.ManterQuestionarioAnamnese;
import br.ueg.openodonto.util.bean.QuestionarioQuestaoAdapter;

public class QuestaoQuestionarioConverter implements Converter{

	
	private Map<String,Integer> 	indexMap;
	private boolean 				isIndexed;

	private static final int INDEX_REAL_FIRST_LETTER = 1; 
    private static final int INDEX_REAL_SECOND_LETTER = 2; 
    private static final int INDEX_REAL_DEFAULT = 0;
	
	private Map<String, Integer> createIndexMap(UIOrderingList orderingList, Map<String, String[]> requestParameterValuesMap, String clientId) {
        Map<String, Integer> indexMap = new HashMap<>();
        
        String[] strings = requestParameterValuesMap.get(clientId);
        if (strings != null && strings.length != 0) {
            for (String string : strings) {
                boolean firstCharIsLetter = Character.isLetter(string.charAt(0));
                boolean secondCharIsLetter = Character.isLetter(string.charAt(1));
                int indexDivisor = string.indexOf(":");
                int indexReal = firstCharIsLetter ? (secondCharIsLetter ? INDEX_REAL_SECOND_LETTER : INDEX_REAL_FIRST_LETTER) : INDEX_REAL_DEFAULT;
                int indexVirtual = indexDivisor + 1;
                Integer real = Integer.valueOf(string.substring(indexReal, indexDivisor));
                String virtual = string.substring(indexVirtual);
                indexMap.put(virtual, real);
            }
        }
        return indexMap;
    }
	
	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		if(!isIndexed){
			doIndexMap(context,component);
		}
		Integer id = indexMap.get(value);		
		ManterQuestionarioAnamnese mBean = getMbean(context);		
		List<QuestionarioQuestaoAdapter> questoes =  mBean.getManageQuestao().getQuestoesAdapter();
		QuestionarioQuestaoAdapter questao = questoes.get(id);
		return questao;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		QuestionarioQuestaoAdapter adapter = (QuestionarioQuestaoAdapter) value;
		return adapter.getQqa().getIndex().toString();
	}
	
	private ManterQuestionarioAnamnese getMbean(FacesContext context){
		ELContext elctx = context.getELContext();
		ManterQuestionarioAnamnese mBean = (ManterQuestionarioAnamnese)elctx.getELResolver().getValue(elctx, null, "manterQuestionarioAnamnese");
		return mBean;
	}

}
