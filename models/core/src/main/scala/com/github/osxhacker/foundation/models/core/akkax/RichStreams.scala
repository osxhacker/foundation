/**
 * MIT License
 * 
 * Copyright (c) 2021 osxhacker
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
*/

package com.github.osxhacker.foundation.models.core.akkax

import scala.collection.generic.CanBuildFrom
import scala.concurrent.Future
import scala.concurrent.duration.{
	Deadline,
	FiniteDuration
	}

import scala.language.{
	higherKinds,
	implicitConversions,
	postfixOps
	}

import scala.reflect.ClassTag

import akka.NotUsed
import akka.actor.ActorRef
import akka.stream._
import akka.stream.scaladsl._
import akka.util.Timeout
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import scalaz.syntax.Ops

import com.github.osxhacker.foundation.models.core.akkax.error.StreamError
import com.github.osxhacker.foundation.models.core.error.ApplicationError
import com.github.osxhacker.foundation.models.core.functional._


/**
 * The '''RichFlow''' type defines enrichments to the OEM functionality
 * provided by the
 * [[https://doc.akka.io/api/akka/current/akka/stream/scaladsl/Flow.html Flow]]
 * [[https://doc.akka.io/docs/akka/current/stream/index.html?language=scala Akka Streams]]
 * type.
 *
 * @author osxhacker
 *
 */
sealed class RichFlow[In, Out, Mat]
{
	/**
	 * The foldMap method provides the same contract as
	 * [[scalaz.Foldable.foldMap]] for [[akka.stream.scaladsl.Flow]] types
	 * in that it maps each element produced by the '''flow''' to the
	 * [[scalaz.Monoid]] for ''A''.
	 */
	def foldMap[A] (flow : Flow[In, Out, Mat])
		(f : Out => A)
		(implicit M : Monoid[A])
		: flow.Repr[A] =
		flow.fold (M.zero) {
			(accum, out) =>

			M.append (accum, f (out));
			}


	/**
	 * The mapExpand method is provided for semantic clarity and is defined in
	 * terms of `flatMapConcat`.
	 */
	def mapExpand[A] (flow : Flow[In, Out, Mat])
		(f : Out => Graph[SourceShape[A], Mat])
		: flow.Repr[A] =
		flow.flatMapConcat (f);


	/**
	 * The mapReduce method is an alias for `foldMap`, provided for semantic
	 * clarity.
	 */
	def mapReduce[A] (flow : Flow[In, Out, Mat])
		(f : Out => A)
		(implicit M : Monoid[A])
		: flow.Repr[A] =
		foldMap[A] (flow) (f);


	/**
	 * The requestOf method uses a functor '''f''' to produce a concrete
	 * [[com.github.osxhacker.foundation.models.core.akkax.Request]] which is sent to the
	 * '''actor''', expected to produce the
	 * [[com.github.osxhacker.foundation.models.core.akkax.Response]] within the
	 * [[scala.concurrent.duration.FiniteDuration]] provided.
	 */
	def requestOf[RespT <: Response[RespT]] (flow : Flow[In, Out, Mat])
		(actor : => ActorRef)
		(f : Out => Request[RespT])
		(implicit D : FiniteDuration, CT : ClassTag[RespT])
		: flow.Repr[RespT] =
		flow.map (f).ask[RespT] (actor) (Timeout (D), CT);


	/**
	 * The valueOrFail method produces the `right` value held in a
	 * [[scalaz.\/]] if that's what is present or fails the '''flow''' with an
	 * exception.
	 */
	def valueOrFail[A0 <: Throwable, B0] (flow : Flow[In, Out, Mat])
		(
			implicit MAB : Unapply2[Bifunctor, Out] {
				type A = A0
				type B = B0
				},

			l : Leibniz.===[Out, A0 \/ B0]
		)
		: flow.Repr[B0] =
		flow.map (l (_).valueOr (err => throw err));
}


object RichFlow
	extends RichFlowImplicits
{
	@inline
	def apply[In, Out, Mat] ()
		(implicit rf : RichFlow[In, Out, Mat])
		: RichFlow[In, Out, Mat] =
		rf;
}


/**
 * The '''RichSource''' type defines enrichments to the OEM functionality
 * provided by the
 * [[https://doc.akka.io/api/akka/current/akka/stream/scaladsl/Source.html Source]]
 * [[https://doc.akka.io/docs/akka/current/stream/index.html?language=scala Akka Streams]]
 * type.
 *
 * @author osxhacker
 *
 */
sealed class RichSource[Out, Mat]
{
	/// Class Imports
	import EitherT.eitherT
	import futures.FutureEither


	/**
	 * The foldMap method provides the same contract as
	 * [[scalaz.Foldable.foldMap]] for [[akka.stream.scaladsl.Source]] types
	 * in that it maps each element produced by the '''source''' to the
	 * [[scalaz.Monoid]] for ''A''.
	 */
	def foldMap[A] (source : Source[Out, Mat])
		(f : Out => A)
		(implicit M : Monoid[A])
		: source.Repr[A] =
		source.fold (M.zero) {
			(accum, out) =>

			M.append (accum, f (out));
			}


	/**
	 * The futureEither method attempts to produce '''one''' ''Out'' instance
	 * within the [[com.github.osxhacker.foundation.models.core.functional.FutureEither]]
	 * container by accessing the `head` element.  If there are no ''Out''
	 * instances or the '''source''' fails, the
	 * [[com.github.osxhacker.foundation.models.core.functional.FutureEither]] will
	 * complete with an ''-\/'' having the error.
	 */
	def futureEither (source : Source[Out, Mat])
		(implicit mat : Materializer)
		: FutureEither[Out] =
		eitherT {
			source.map[ApplicationError \/ Out] (a => \/- (a))
				.recover (mapError[Out] ())
				.orElse (
					Source.single[ApplicationError \/ Out] (
						-\/ (StreamError ("underflow", None))
						)
					)
				.toMat (Sink.head[ApplicationError \/ Out]) (Keep.right)
				.run ();
			}


	/**
	 * The futureEitherM method attempts to produce as many ''Out'' instances
	 * within the [[com.github.osxhacker.foundation.models.core.functional.FutureEither]]
	 * container as the [[scalaz.Monoiad]] ''Monoid[M[Out]]'' type can contain.
	 * If there are no ''Out'' instances or the '''source''' fails, the
	 * [[com.github.osxhacker.foundation.models.core.functional.FutureEither]] will
	 * complete with an ''-\/'' having the error.
	 */
	def futureEitherM[M[_]] (source : Source[Out, Mat])
		(implicit mat : Materializer, A : Applicative[M], MO : Monoid[M[Out]])
		: FutureEither[M[Out]] =
		eitherT {
			foldMap[M[Out]] (source) (a => A.point (a))
				.map (os => \/- (os))
				.orElse (Source.single (\/- (MO.zero)))
				.recover (mapError[M[Out]] ())
				.toMat (Sink.head[ApplicationError \/ M[Out]]) (Keep.right)
				.run ();
			}


	/**
	 * The futureEitherCollection method attempts to produce as many ''Out''
	 * instances within the
	 * [[com.github.osxhacker.foundation.models.core.functional.FutureEither]] container
	 * as the '''source''' can produce, held within the newly minted
	 * ''C[Out]''.  If there are no ''Out'' instances or the '''source'''
	 * fails, the [[com.github.osxhacker.foundation.models.core.functional.FutureEither]]
	 * will complete with an ''-\/'' having the error.
	 */
	def futureEitherCollection[C[_]] (source : Source[Out, Mat])
		(implicit cbf : CanBuildFrom[Nothing, Out, C[Out]], mat : Materializer)
		: FutureEither[C[Out]] =
		eitherT {
			source.fold (cbf ()) {
				(builder, o) =>

				builder += o;
				}
				.map (os => \/- (os.result ()))
				.orElse (Source.single (\/- (cbf ().result ())))
				.recover (mapError[C[Out]] ())
				.toMat (Sink.head[ApplicationError \/ C[Out]]) (Keep.right)
				.run ();
			}


	/**
	 * The mapExpand method is provided for semantic clarity and is defined in
	 * terms of `flatMapConcat`.
	 */
	def mapExpand[A] (source : Source[Out, Mat])
		(f : Out => Graph[SourceShape[A], Mat])
		: source.Repr[A] =
		source.flatMapConcat (f);


	/**
	 * The mapReduce method is an alias for `foldMap`, provided for semantic
	 * clarity.
	 */
	def mapReduce[A] (source : Source[Out, Mat])
		(f : Out => A)
		(implicit M : Monoid[A])
		: source.Repr[A] =
		foldMap[A] (source) (f);


	/**
	 * The requestOf method uses a functor '''f''' to produce a concrete
	 * [[com.github.osxhacker.foundation.models.core.akkax.Request]] which is sent to the
	 * '''actor''', expected to produce the
	 * [[com.github.osxhacker.foundation.models.core.akkax.Response]] within the
	 * [[scala.concurrent.duration.FiniteDuration]] provided.
	 */
	def requestOf[RespT <: Response[RespT]] (source : Source[Out, Mat])
		(actor : => ActorRef)
		(f : Out => Request[RespT])
		(implicit D : FiniteDuration, CT : ClassTag[RespT])
		: source.Repr[RespT] =
		source.map (f).ask[RespT] (actor) (Timeout (D), CT);


	/**
	 * The valueOrFail method produces the `right` value held in a
	 * [[scalaz.\/]] if that's what is present or fails the '''source''' with an
	 * exception.
	 */
	def valueOrFail[A0 <: Throwable, B0] (source : Source[Out, Mat])
		(
			implicit MAB : Unapply2[Bifunctor, Out] {
				type A = A0
				type B = B0
				},

			l : Leibniz.===[Out, A0 \/ B0]
		)
		: source.Repr[B0] =
		source.map (l (_).valueOr (err => throw err));


	private def mapError[T] ()
		: PartialFunction[Throwable, ApplicationError \/ T] =
	{
		case ae : ApplicationError =>
			-\/ (ae);
			
		case e : Throwable =>
			-\/ (StreamError (e));
	}
}


object RichSource
	extends RichSourceImplicits
{
	@inline
	def apply[Out, Mat] ()
		(implicit rs : RichSource[Out, Mat])
		: RichSource[Out, Mat] =
		rs;
}


trait RichFlowImplicits
{
	/// Implicit Conversions
	implicit def RichFlowImplicit[In, Out, Mat] : RichFlow[In, Out, Mat] =
		new RichFlow[In, Out, Mat] ();
}


trait RichSourceImplicits
{
	/// Implicit Conversions
	implicit def RichSourceImplicit[Out, Mat] : RichSource[Out, Mat] =
		new RichSource[Out, Mat] ();
}


/**
 * The '''MapExpander''' type defines a type class which allows the `mapExpand`
 * methods to select the most appropriate [[akka.streams.Graph]] factory based
 * on the precise result of a (supported) functor's return type.
 * 
 * @author osxhacker
 * 
 */
sealed trait MapExpander[-A, +B]
{
	def createGraphFrom (a : A) : Graph[SourceShape[B], NotUsed];
}


object MapExpander
{
	/// Class Imports
	import scala.collection.immutable


	/// Implicit Conversions
	implicit def MapExpanderIterable[A, C[A] <: immutable.Iterable[A]]
		: MapExpander[C[A], A] =
		new MapExpander[C[A], A] {
			override def createGraphFrom (a : C[A])
				: Graph[SourceShape[A], NotUsed] =
				Source (a);
			}


	implicit def MapExpanderIterator[A]
		: MapExpander[Iterator[A], A] =
		new MapExpander[Iterator[A], A] {
			override def createGraphFrom (a : Iterator[A])
				: Graph[SourceShape[A], NotUsed] =
				Source.fromIterator (() => a);
			}


	implicit def MapExpanderGraph[A]
		: MapExpander[Graph[SourceShape[A], NotUsed], A] =
		new MapExpander[Graph[SourceShape[A], NotUsed], A] {
			override def createGraphFrom (a : Graph[SourceShape[A], NotUsed])
				: Graph[SourceShape[A], NotUsed] =
				a;
			}


	implicit def MapExpanderSource[A]
		: MapExpander[Source[A, NotUsed], A] =
		new MapExpander[Source[A, NotUsed], A] {
			override def createGraphFrom (a : Source[A, NotUsed])
				: Graph[SourceShape[A], NotUsed] =
				a;
			}
}


final class FlowOps[In, Out, Mat] (override val self : Flow[In, Out, Mat])
	(implicit RF : RichFlow[In, Out, Mat])
	extends Ops[Flow[In, Out, Mat]]
{
	/// Class Types
	final class FlowRequestOfClient[MEL <: MessagingExtensionLike] (
		private val client : MEL
		)
	{
		def apply[RespT <: client.ResponseType[RespT]] (
			f : Out => client.RequestType[RespT]
			)
			(implicit D : FiniteDuration, ct : ClassTag[RespT])
			: self.Repr[RespT] =
			client.withActor {
				target =>

				implicit val timeout = Timeout (D);

				self.map (f).ask[RespT] (target);
				}
	}


	def foldMap[A : Monoid] (f : Out => A) : self.Repr[A] =
		RF.foldMap[A] (self) (f);


	def mapReduce[A : Monoid] (f : Out => A) : self.Repr[A] =
		RF.mapReduce[A] (self) (f);


	def requestOf[RespT <: Response[RespT]] (actor : => ActorRef)
		(f : Out => Request[RespT])
		(implicit D : FiniteDuration, CT : ClassTag[RespT])
		: self.Repr[RespT] =
		RF.requestOf[RespT] (self) (actor) (f);


	def requestOfClient[MEL <: MessagingExtensionLike] (client : => MEL)
		: FlowRequestOfClient[MEL] =
		new FlowRequestOfClient (client);


	def valueOrFail[A0 <: Throwable, B0] ()
		(
			implicit MAB : Unapply2[Bifunctor, Out] {
				type A = A0
				type B = B0
				},

			l : Leibniz.===[Out, A0 \/ B0]
		)
		: self.Repr[B0] =
		RF.valueOrFail[A0, B0] (self);
}


final class FlowWithNotUsedOps[In, Out] (
	override val self : Flow[In, Out, NotUsed]
	)
	(implicit RF : RichFlow[In, Out, NotUsed])
	extends Ops[Flow[In, Out, NotUsed]]
{
	/// Class Imports
	import scala.collection.immutable


	/**
	 * The mapExpand method delegates the creation of a [[akka.streams.Graph]]
	 * to [[com.github.osxhacker.foundation.models.core.akkax.MapExpander]] so that the
	 * proper [[akka.streams.scaladsl.Source]] can be employed.
	 */
	def mapExpand[A, B] (f : Out => A)
		(implicit ME : MapExpander[A, B])
		: self.Repr[B] =
		RF.mapExpand[B] (self) (out => ME.createGraphFrom (f (out)));
}


final class SourceOps[Out, Mat] (override val self : Source[Out, Mat])
	(implicit RS : RichSource[Out, Mat], m : Materializer)
	extends Ops[Source[Out, Mat]]
{
	/// Class Imports
	import futures.FutureEither


	/// Class Types
	final class SourceRequestOfClient[MEL <: MessagingExtensionLike] (
		private val client : MEL
		)
	{
		def apply[RespT <: client.ResponseType[RespT]] (
			f : Out => client.RequestType[RespT]
			)
			(implicit D : FiniteDuration, ct : ClassTag[RespT])
			: self.Repr[RespT] =
			client.withActor {
				target =>

				implicit val timeout = Timeout (D);

				self.map (f).ask[RespT] (target);
				}
	}


	def foldMap[A : Monoid] (f : Out => A) : self.Repr[A] =
		RS.foldMap[A] (self) (f);


	def mapReduce[A : Monoid] (f : Out => A) : self.Repr[A] =
		RS.mapReduce[A] (self) (f);


	def requestOf[RespT <: Response[RespT]] (actor : => ActorRef)
		(f : Out => Request[RespT])
		(implicit D : FiniteDuration, CT : ClassTag[RespT])
		: self.Repr[RespT] =
		RS.requestOf[RespT] (self) (actor) (f);


	def requestOfClient[MEL <: MessagingExtensionLike] (client : => MEL)
		: SourceRequestOfClient[MEL] =
		new SourceRequestOfClient (client);


	def toFutureEither () : FutureEither[Out] =
		RS.futureEither (self);


	def toFutureEitherM[M[_]] ()
		(implicit A : Applicative[M], MO : Monoid[M[Out]])
		: FutureEither[M[Out]] =
		RS.futureEitherM[M] (self);


	def toFutureEitherCollection[C[_]] ()
		(implicit cbf : CanBuildFrom[Nothing, Out, C[Out]])
		: FutureEither[C[Out]] =
		RS.futureEitherCollection[C] (self);


	def valueOrFail[A0 <: Throwable, B0] ()
		(
			implicit MAB : Unapply2[Bifunctor, Out] {
				type A = A0
				type B = B0
				},

			l : Leibniz.===[Out, A0 \/ B0]
		)
		: self.Repr[B0] =
		RS.valueOrFail[A0, B0] (self);
}


final class SourceWithNotUsedOps[Out] (override val self : Source[Out, NotUsed])
	(implicit RS : RichSource[Out, NotUsed], m : Materializer)
	extends Ops[Source[Out, NotUsed]]
{
	/// Class Imports
	import futures.FutureEither
	import scala.collection.immutable


	/// Class Types
	final class SourceRequestOfClient[MEL <: MessagingExtensionLike] (
		private val client : MEL
		)
	{
		def apply[RespT <: client.ResponseType[RespT]] (
			f : Out => client.RequestType[RespT]
			)
			(implicit D : FiniteDuration, ct : ClassTag[RespT])
			: self.Repr[RespT] =
			client.withActor {
				target =>

				implicit val timeout = Timeout (D);

				self.map (f).ask[RespT] (target);
				}
	}


	/**
	 * The mapExpand method delegates the creation of a [[akka.streams.Graph]]
	 * to [[com.github.osxhacker.foundation.models.core.akkax.MapExpander]] so that the
	 * proper [[akka.streams.scaladsl.Source]] can be employed.
	 */
	def mapExpand[A, B] (f : Out => A)
		(implicit ME : MapExpander[A, B])
		: self.Repr[B] =
		RS.mapExpand[B] (self) (out => ME.createGraphFrom (f (out)));


	def requestOf[RespT <: Response[RespT]] (actor : => ActorRef)
		(f : Out => Request[RespT])
		(implicit D : FiniteDuration, CT : ClassTag[RespT])
		: self.Repr[RespT] =
		RS.requestOf[RespT] (self) (actor) (f);


	def requestOfClient[MEL <: MessagingExtensionLike] (client : => MEL)
		: SourceRequestOfClient[MEL] =
		new SourceRequestOfClient (client);


	def toFutureEither () : FutureEither[Out] =
		RS.futureEither (self);


	def toFutureEitherM[M[_]] ()
		(implicit A : Applicative[M], MO : Monoid[M[Out]])
		: FutureEither[M[Out]] =
		RS.futureEitherM[M] (self);


	def toFutureEitherCollection[C[_]] ()
		(implicit cbf : CanBuildFrom[Nothing, Out, C[Out]])
		: FutureEither[C[Out]] =
		RS.futureEitherCollection[C] (self);


	def valueOrFail[A0 <: Throwable, B0] ()
		(
			implicit MAB : Unapply2[Bifunctor, Out] {
				type A = A0
				type B = B0
				},

			l : Leibniz.===[Out, A0 \/ B0]
		)
		: self.Repr[B0] =
		RS.valueOrFail[A0, B0] (self);
}


trait ToFlowOps
{
	/// Implicit Conversions
	implicit def FromFlow[In, Out] (f : Flow[In, Out, NotUsed])
		(implicit RS : RichFlow[In, Out, NotUsed])
		=
		new FlowWithNotUsedOps[In, Out] (f);


	implicit def FromFlowAndMat[In, Out, Mat] (f : Flow[In, Out, Mat])
		(implicit RF : RichFlow[In, Out, Mat])
		=
		new FlowOps (f);
}


trait ToSourceOps
{
	/// Implicit Conversions
	implicit def FromSource[Out] (s : Source[Out, NotUsed])
		(implicit RS : RichSource[Out, NotUsed], m : Materializer)
		=
		new SourceWithNotUsedOps[Out] (s);


	implicit def FromSourceAndMat[Out, Mat] (s : Source[Out, Mat])
		(implicit RS : RichSource[Out, Mat], m : Materializer)
		=
		new SourceOps[Out, Mat] (s);
}
