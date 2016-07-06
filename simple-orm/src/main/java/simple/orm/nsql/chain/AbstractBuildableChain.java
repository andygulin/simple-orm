package simple.orm.nsql.chain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import simple.orm.meta.EntityMetadata;
import simple.orm.nsql.cmd.SQLCommand;

public abstract class AbstractBuildableChain extends AbstractChain implements BuildableChain {

	public AbstractBuildableChain() {
		super();
	}

	public AbstractBuildableChain(AbstractChain previous) {
		super(previous);
	}

	@Override
	public SQLCommand build() {
		StringBuffer sb = new StringBuffer();

		List<Object> params = null;

		Map<String, Class<?>> classMap = new HashMap<>();

		while (CollectionUtils.isNotEmpty(this.chains)) {
			AbstractChain ch = this.chains.pop();
			ch.appendTo(sb);
			if (ch instanceof WhereChain) {
				params = ((WhereChain) ch).params;
			} else if (ch instanceof JoinChain) {
				JoinChain foo = (JoinChain) ch;
				EntityMetadata<?> em = EntityMetadata.newInstance(foo.clazz);
				classMap.put(em.getTableName(), foo.clazz);
			} else if (ch instanceof FromChain) {
				FromChain foo = (FromChain) ch;
				EntityMetadata<?> em = EntityMetadata.newInstance(foo.clazz);
				classMap.put(em.getTableName(), foo.clazz);
			}
		}
		return new SQLCommand(sb.toString(), params, classMap);
	}
}
