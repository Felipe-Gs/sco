package br.ueg.openodonto.validator;

import java.util.List;
import java.util.Map;

import br.com.vitulus.simple.jdbc.Entity;
import br.com.vitulus.simple.jdbc.EntityManager;
import br.com.vitulus.simple.jdbc.orm.old.OrmFormat;
import br.com.vitulus.simple.jdbc.orm.old.OrmResolver;
import br.com.vitulus.simple.jdbc.orm.old.OrmTranslator;
import br.com.vitulus.simple.jdbc.sql.old.CrudQuery;
import br.com.vitulus.simple.jdbc.sql.old.IQuery;
import br.com.vitulus.simple.validator.AbstractValidator;
import br.com.vitulus.simple.validator.CpfValidator;
import br.com.vitulus.simple.validator.tipo.ObjectValidatorType;
import br.ueg.openodonto.controle.ManageBeanGeral;
import br.ueg.openodonto.dominio.constante.PessoaFisica;
import br.ueg.openodonto.util.WordFormatter;

public class UniqueCpfValidator<T extends Entity & PessoaFisica<T>> extends AbstractValidator implements ObjectValidatorType {
    private static final String ERROR_MSG = "CPF já está sendo usado.";

    public UniqueCpfValidator(T value) {
        super(value);
    }

    @Override
    protected boolean validate() {
        T value = getValue();
        String cpf = WordFormatter.clear(value.getCpf());
        UniqueCpfChecker<T> checker = new UniqueCpfChecker<>(value.getClass(), cpf);
        return checker.check();
    }

    private static class UniqueCpfChecker<T extends Entity & PessoaFisica<T>> {
        private final Class<T> type;
        private final String cpf;

        public UniqueCpfChecker(Class<T> type, String cpf) {
            this.type = type;
            this.cpf = cpf;
        }

        public boolean check() {
            EntityManager<T> dao = ManageBeanGeral.getDao(type);
            List<Map<String, Object>> result = getQueryResult(dao);
            if (result.size() == 1) {
                return isSamePf(result.get(0));
            } else if (result.size() > 1) {
                throw new IllegalStateException("Falha de integridade. Permitido apenas uma pessoa por CPF.");
            }
            return true;
        }

        private List<Map<String, Object>> getQueryResult(EntityManager<T> dao) {
            IQuery query = createQuery(dao);
            try {
                return dao.getSqlExecutor().executarUntypedQuery(query);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private IQuery createQuery(EntityManager<T> dao) {
            Map<String, Object> params = Map.of("cpf", cpf);
            List<String> keyFieldNames = OrmResolver.getKeyFieldNames(type, true);
            String[] fields = keyFieldNames.toArray(new String[0]);
            return CrudQuery.getSelectQuery(type, params, fields);
        }

        private boolean isSamePf(Map<String, Object> already) {
            T value = getValue();
            OrmFormat format = new OrmFormat(value);
            List<String> keyFieldNames = OrmResolver.getKeyFieldNames(type, true);
            Map<String, Object> local = format.format(keyFieldNames.toArray(new String[0]));
            OrmTranslator translator = new OrmTranslator(type, keyFieldNames);
            for (String fieldName : keyFieldNames) {
                Object alreadyValue = already.get(translator.getColumn(fieldName));
                Object localValue = local.get(fieldName);
                if (!alreadyValue.equals(localValue)) {
                    setErrorMsg(ERROR_MSG);
                    return false;
                }
            }
            return true;
        }
    }
}
