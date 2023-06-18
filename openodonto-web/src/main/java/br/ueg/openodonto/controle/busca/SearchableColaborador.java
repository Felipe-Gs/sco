package br.ueg.openodonto.controle.busca;

import java.lang.reflect.Field;
import java.util.ArrayList;

import br.com.vitulus.simple.jdbc.orm.old.OrmResolver;
import br.com.vitulus.simple.jdbc.orm.old.OrmTranslator;
import br.com.vitulus.simple.validator.EmptyValidator;
import br.com.vitulus.simple.validator.NullValidator;
import br.com.vitulus.simple.validator.Validator;
import br.ueg.openodonto.controle.servico.ExampleRequest;
import br.ueg.openodonto.dominio.Colaborador;
import br.ueg.openodonto.dominio.Paciente;
import br.ueg.openodonto.dominio.Produto;
import br.ueg.openodonto.dominio.constante.CategoriaProduto;
import br.ueg.openodonto.persistencia.dao.sql.SqlWhereOperatorType;
import br.ueg.openodonto.servico.busca.FieldFacade;
import br.ueg.openodonto.servico.busca.MessageDisplayer;
import br.ueg.openodonto.servico.busca.SearchFilterBase;
import br.ueg.openodonto.validator.ValidatorFactory;

public class SearchableColaborador extends AbstractSearchable<Colaborador> {

	private static final long serialVersionUID = -2595474489456158101L;

	private CategoriaProduto categoria;
	
	public SearchableColaborador(MessageDisplayer displayer) {
		this(displayer,null);
	}
	
	public SearchableColaborador(MessageDisplayer displayer,CategoriaProduto categoria) {
		super(Colaborador.class,displayer);
		this.categoria = categoria;
	}

	public void buildFacade(){
		super.buildFacade();
		OrmTranslator translator = new OrmTranslator(OrmResolver.getAllFields(new ArrayList<Field>(), Paciente.class, true));
		getFacade().add(new FieldFacade("Código",translator.getColumn("codigo")));
		getFacade().add(new FieldFacade("Nome",translator.getColumn("nome")));
		getFacade().add(new FieldFacade("Email",translator.getColumn("email")));
		getFacade().add(new FieldFacade("CPF/CNPJ","documento"));
	}
	
	public void buildFilter(){
		super.buildFilter();
		buildNameFilter();
		buildDescricaoFilter();
		buildProdutoNomeFilter();
		buildProdutoDescricaoFilter();
	}
	private static final int DESCRICAO_FILTER_MAX_LENGTH = 300;
	private static final int DESCRICAO_FILTER_MIN_LENGTH = 5;
	private static final int NOME_FILTER_MAX_LENGTH = 150;
	private static final int NOME_FILTER_MIN_LENGTH = 3;
	private static final int PRODUTO_NOME_FILTER_MAX_LENGTH = 100;
	private static final int PRODUTO_NOME_FILTER_MIN_LENGTH = 3;
	private static final int PRODUTO_DESCRICAO_FILTER_MAX_LENGTH = 300;
	private static final int PRODUTO_DESCRICAO_FILTER_MIN_LENGTH = 3;

	private void buildDescricaoFilter() {
		Validator validator = ValidatorFactory.newStrRangeLen(DESCRICAO_FILTER_MAX_LENGTH, DESCRICAO_FILTER_MIN_LENGTH, true);
		getFiltersMap().put("descricaoFilter", buildBasicFilter("descricaoFilter","Descrição",validator));
	}

	private void buildNameFilter() {
		Validator validator = ValidatorFactory.newStrRangeLen(NOME_FILTER_MAX_LENGTH, NOME_FILTER_MIN_LENGTH, true);
		getFiltersMap().put("nomeFilter", buildBasicFilter("nomeFilter","Nome",validator));
	}

	private void buildProdutoNomeFilter() {
		Validator validator = ValidatorFactory.newStrRangeLen(PRODUTO_NOME_FILTER_MAX_LENGTH, PRODUTO_NOME_FILTER_MIN_LENGTH, true);
		getFiltersMap().put("produtoNomeFilter", buildBasicFilter("produtoNomeFilter","Nome produto",validator));
	}

	private void buildProdutoDescricaoFilter() {
		Validator validator = ValidatorFactory.newStrRangeLen(PRODUTO_DESCRICAO_FILTER_MAX_LENGTH, PRODUTO_DESCRICAO_FILTER_MIN_LENGTH, true);
		SearchFilterBase produtoDescricaoFilter = buildBasicFilter("produtoDescricaoFilter", "Descrição produto", validator);
		getFiltersMap().put("produtoDescricaoFilter", produtoDescricaoFilter);
	}
	
	public CategoriaProduto getCategoria() {
		return categoria;
	}

	public void setCategoria(CategoriaProduto categoria) {
		this.categoria = categoria;
	}

	public Produto buildExampleProduto(){
		Object nome = getFiltersMap().get("produtoNomeFilter").getField().getInputFields().get(0).getValue();
		Object desc = getFiltersMap().get("produtoDescricaoFilter").getField().getInputFields().get(0).getValue();
		Produto produto = null;
		if(nome != null && !nome.toString().trim().isEmpty()){
			produto = new Produto();
		    produto.setNome("%"+nome.toString()+"%");
		}
		if(desc != null && !desc.toString().trim().isEmpty()){
			produto = produto == null ? new Produto() : produto;
		    produto.setDescricao("%"+desc.toString()+"%");
		}
	    return produto;	
	}
	
	@Override
	public Colaborador buildExample() {
		ExampleRequest<Colaborador> request  = new ExampleRequest<Colaborador>(this);		
		request.getFilterRelation().add(request.new TypedFilter("nomeFilter", "nome",SqlWhereOperatorType.LIKE));
		request.getFilterRelation().add(request.new TypedFilter("descricaoFilter","descricao",SqlWhereOperatorType.LIKE));
		request.getInvalidPermiteds().add(NullValidator.class);
		request.getInvalidPermiteds().add(EmptyValidator.class);
		Colaborador target = getManageExample().processExampleRequest(request);
		target.setCategoria(categoria);
		return target;
	}

}
