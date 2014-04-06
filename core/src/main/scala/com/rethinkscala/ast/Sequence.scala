package com.rethinkscala.ast

import com.rethinkscala._
import com.rethinkscala.BoundOptions
import scala.Some
import com.rethinkscala.japi.ReductionFunction
import com.rethinkscala.magnets.GroupFilterMagnet

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 2/24/14
 * Time: 8:15 AM 
 */


object Sequence {
  implicit def mapDocumentToSequence[T <: Document, ST, S[ST] <: Sequence[ST]] = CanMap[T, S[ST], ST]


}

trait Sequence[T] extends Multiply with Filterable[T] with Record  {


  override val underlying = this


  def indexesOf[R >: Datum](value: R) = IndexesOf(underlying, value)

  //def indexesOf(value: Binary): IndexesOf = indexesOf((x: Var) => value)

  def isEmpty = IsEmpty(underlying)

  def sample(amount: Int) = Sample(underlying, amount)

  def indexesOf(p: Var => Binary) = IndexesOf(underlying, p)

  def apply(index: Int) = Nth(underlying, index)

  def skip(amount: Int) = Skip(underlying, amount)

  def limit(amount: Int) = Limit(underlying, amount)

  def slice(start: Int = 0, end: Int = -1) = Slice(underlying, start, end, BoundOptions())

  def slice(start: Int, end: Int, bounds: BoundOptions) = if (end > 0) Slice(underlying, start, end, bounds)
  else
    Slice(underlying, List(start, end).max, -1, BoundOptions(rightBound = Some(Bound.Closed)))


  def apply(range: SliceRange) = slice(range.start, range.end)

  def union(sequence: Sequence[_]) = Union(underlying, sequence)

  def ++(sequence: Sequence[_]) = union(sequence)

  def eqJoin[R](attr: String, other: Sequence[R], index: Option[String] = None) = EqJoin(underlying, attr, other, index)

  def innerJoin[R](other: Sequence[R], func: (Var, Var) => Binary) = InnerJoin[T, R](underlying, other, func)

  def outerJoin[R](other: Sequence[R], func: (Var, Var) => Binary) = OuterJoin[T, R](underlying, other, func)




  def orderByIndex(index: String): OrderBy[T] = orderByIndex(index: Order)

  def orderByIndex(index: Order): OrderBy[T] = OrderBy[T](underlying, Seq(), Some(index))

  def orderBy(keys: Order*) = OrderBy[T](underlying, keys)

  def withFields(keys: Any*) = WithFields(underlying, keys)

  def size = count

  def count = Count(underlying)

  def count(value: Datum) = Count(underlying, Some(FuncWrap(value)))

  def count(f: Var => Binary) = Count(underlying, Some(FuncWrap(f)))


  def group[R](magnet: GroupFilterMagnet[R]): Group[R, T] = Group(underlying, magnet().map(FuncWrap(_)))

  ///


 // def groupBy(method: AggregateByMethod, attrs: String*) = GroupBy(underlying, method, attrs)

  def distinct = Distinct(underlying)

  //def contains(attrs: Datum*) = Contains(underlying, attrs)

  def contains[R >: FilterTyped](attrs: R*) = Contains(underlying, attrs.map(FuncWrap(_)))

  def ?(attr: Datum) = contains(attr)

  // add dummy implicit to allow methods for Ref




  def foreach(f: Var => Typed) = ForEach(underlying, f)

  def max()=Max(underlying)
  def max(field:String) = Max(underlying,Some(field))

  def max(f:Predicate1) = Max(underlying,Some(f))

  def min()=Max(underlying)
  def min(field:String) = Min(underlying,Some(field))

  def min(f:Predicate1) = Min(underlying,Some(f))

}


trait Stream[T] extends Sequence[T]

trait Selection[T] extends Sequence[T] {

  override val underlying = this


  def update(attributes: Map[String, Any]): Update[T] = update(attributes, UpdateOptions())

  def update(attributes: Map[String, Any], options: UpdateOptions) = Update[T](underlying, FuncWrap(attributes), options)

  def update(p: Predicate1): Update[T] = update(p, UpdateOptions())

  def update(p: Predicate1, options: UpdateOptions) = Update[T](underlying, FuncWrap(p), options)

  def update(d: Document): Update[T] = update((x: Var) => MakeObj2(d))

  def update(d: Document, options: UpdateOptions): Update[T] = Update[T](underlying, FuncWrap(MakeObj2(d)), options)


  def replace(p: Predicate1): Replace[T] = replace(p, UpdateOptions())

  def replace(p: Predicate1, options: UpdateOptions): Replace[T] = Replace(underlying, p, options)

  def replace(d: Document): Replace[T] = replace(d, UpdateOptions())

  def replace(d: Document, options: UpdateOptions): Replace[T] = Replace(underlying, MakeObj2(d), options)

  def replace(data: Map[String, Any]): Replace[T] = replace(data, UpdateOptions())

  def replace(data: Map[String, Any], options: UpdateOptions): Replace[T] = Replace(underlying, FuncWrap(data), options)

  def delete: Delete[T] = delete()

  def delete(durability: Option[Durability.Kind] = None): Delete[T] = Delete(underlying)

}

trait StreamSelection[T] extends Selection[T] with Stream[T] {
  override val underlying = this

  def between(start: Int, stop: Int): Between[T] = between(start, stop, BetweenOptions())

  def between(start: String, stop: String): Between[T] = between(start, stop, BetweenOptions())

  def between(start: Int, stop: Int, options: BetweenOptions) = Between(underlying, start, stop, options)

  def between(start: String, stop: String, options: BetweenOptions) = Between(underlying, start, stop, options)


}

trait SingleSelection[T] extends Selection[T]


trait Filterable[T] extends Typed {

  //def filter(value: Binary): Filter[T] = filter((x: Var) => value)

  override val underlying = this

  def filter(value: Map[String, Any]): Filter[T] = filter(value, false)

  def filter(value: Map[String, Any], default: Boolean): Filter[T] = Filter[T](underlying, FuncWrap(value), default)

  // def filter(value: Typed): Filter[T] = filter(value, false)

  // def filter(value: Typed, default: Boolean): Filter[T] = Filter(this, FuncWrap(value), default)

  def filter(f: Var => Binary): Filter[T] = filter(f, false)

  def filter(f: Var => Binary, default: Boolean): Filter[T] = Filter[T](underlying, FuncWrap(f: ScalaBooleanPredicate1), default)

}
