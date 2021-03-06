# hayde.persistence.manager

i started to write this persistence manager, because other managers had quite difficult and complicated setting environments.

I resignated at a point, where i used hybernate and relized, that connecting to different databases for different 'hosts' (in that case it was a web-service with different agents, running on a single server), was going to drive me crazy.

__Tested databases:__ 

* MySQL 
* PostgreSQL

## usage

### Class preperation
The classes require a 'annotation preperation' for the usage with the manager.

These preperations are seperated into the

* header preperation: general definitions for that class and the table.
* field perperation: informations about the single one variable, that should be joined with a column of a table

#### the header preperation

__@Entity__:

__@Table__:

__@NamedQueries__:


#### the field preperation

__@Column__:

* _name_: (Optional) The name of the column ( in the table! ).
* _unique_: (Optional) Whether the property is a unique key.
* _nullable(default true)_:			 (Optional) Whether the database column is nullable.
* _insertable(default true)_: (Optional) Whether the column is included in SQL INSERT statements generated by the persistence provider.
* _updatable(default true)_:			(Optional) Whether the column is included in SQL UPDATE statements generated by the persistence provider.
* _columnDefinition_: (Optional) The SQL fragment that is used when generating the DDL for the column.
* _table_: (Optional) The name of the table that contains the column.
* _length(default 255)_:			(Optional) The column length.
* _precision(default 0)_:				(Optional) The precision for a decimal (exact numeric) column.


```
	@Column(name = "nodetype_name", unique=true, columnDefinition="varchar(30)", length=30)
	private String name;
```

__@Basic__:
(optional = false)

```
@Basic( optional = false )
private String name;
```

__@Id__:

Id fields are (in general) used as primary key's for that table.
This annotation is quite often used with the next one '@GenerateValue'.

Typically, this field is a 'long' in Java and 'bigint' in the database.

__@GeneratedValue__:
(strategy = TABLE, SEQUENCE, IDENTITY, AUTO)

Id fields should be autogenerated by the database. The strategy is reliable for the type of the generating mechanism. 'AUTO' is the general setting for that field.

(actually i didn't test anything else yet!)

### Example:


```
public class Node {
	public long id;
	public String name;
}
```

```
@Entity
@Table(name = "nodetype")
@NamedQueries({
    @NamedQuery(name = "NodeType.findAll", query = "SELECT * FROM nodetype")})

public class NodeType extends GenericEntityFunctions {

    @Id
    @Basic(optional = false)
    @Column(name = "nodetype_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long id;
    @Column(name = "nodetype_name")
    public String name;

}

```
