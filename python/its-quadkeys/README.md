its-quadkeys
============

This Python-3.10+ package implements usefull abstractions around
quadtrees, suitable for the ITS clients.


its_quadkeys.QuadKey()
----------------------

This class implements one path in a quadtree. I.e. it represents a
n-deep branch of a quadtree. It can be represented as a sequence of
one or more of the characters in the set (0,1,2,3), like: 1332011321

The two main operations on a QuadKey are:

  * its_quadkeys.QuadKey.split(extra_depth):
    Splits the QuadKey into an extra_depth-deeper QuadZone.

  * its_quadkeys.QuadKey.neighbours():
    Returns the QuadKeys neighbouring this QuadKey.


its_quadeys.QuadZone()
----------------------

This class implements a zone made of one or more QuadKeys, and allows
manipulations on that zone.

The two main operations on a QuadZone are:

  * its_quadeys.QuadZone.optimise():
    Merge the QuadKeys defining the Zone, so as to optimise the number
    of QuadKeys defining the zone.
    When a zone is created by adding and removing QuadKeys, the definition
    for that zone can be sub-optimal, like having two QuadKeys partially
    covering the same area, or four QuadKeys that can be merged into their
    super-QuadKey. optimise() allows simplifying the zone.

  * its_quadeys.QuadZone.neighbours(depth):
    Returns a QuadZone made of the neighbouring QuadKeys of deph 'depth'.


Misc thoughts
=============

This section contains various notes that served as a basis for implementing
its-quadkeys; it contains details and explanations that are worth keeping,
but does **not** constitute documentation.

The QuadKeys are organised, starting from the North-West, in the form
of a Z (on the left), not in an inverted-C, a U, a cyrillic И, or of
anything else (on the right):

        Z                   Ↄ           C           U           И
    +---+---+           +---+---+   +---+---+   +---+---+   +---+---+
    | 0 | 1 |           | 0 | 1 |   | 1 | 0 |   | 0 | 3 |   | 0 | 2 |
    +---+---+           +---+---+   +---+---+   +---+---+   +---+---+
    | 2 | 3 |           | 3 | 2 |   | 2 | 3 |   | 1 | 2 |   | 1 | 3 |
    +---+---+           +---+---+   +---+---+   +---+---+   +---+---+
       YES                 NO!         NO!         NO!         ИO!

This gives the following neighbouring relations:

    north_of(0) -> None        north_of(q/0) -> north_of(q)/2
    north_of(1) -> None        north_of(q/1) -> north_of(q)/3
    north_of(2) -> 0           north_of(q/2) -> q/0
    north_of(3) -> 1           north_of(q/3) -> q/1

    south_of(0) -> 2           south_of(q/0) -> q/2
    south_of(1) -> 3           south_of(q/1) -> q/3
    south_of(2) -> None        south_of(q/2) -> south_of(q)/0
    south_of(3) -> None        south_of(q/3) -> south_of(q)/1

    west_of(0) -> 1            west_of(q/0) -> west_of(q)/1
    west_of(1) -> 0            west_of(q/1) -> q/0
    west_of(2) -> 3            west_of(q/2) -> west_of(q)/3
    west_of(3) -> 2            west_of(q/3) -> q/2

    east_of(0) -> 1            east_of(q/0) -> q/1
    east_of(1) -> 0            east_of(q/1) -> east_of(q)/0
    east_of(2) -> 3            east_of(q/2) -> q/3
    east_of(3) -> 2            east_of(q/3) -> east_of(q)/2

--------------------------------------------------------------------------------

There is a fundamental property of QuadKey:
  - if two QuadKeys intersect each other, we can have::
    - the first entirely covers the second, or (non exclusive!)
    - the second entirely covers the first

This means that the intersection of two QuadKeys is either QuadKeys
(the deepest one), never a QuadKey deeper than both.

--------------------------------------------------------------------------------

For a vehicle that is in a quadkey, we want to listen to that
quadkey, as well as all 8 surrounding quadkeys.

There is a simple optimisation that is trivial to implement:
the one-level shallower quadkey contains the quadkey the vehicle
is in, plus three of the surrounding quadkeys, whatever the
deepest tile the vehicle is

For example, assuming the vehicle is in ROOT/0/0/0, then the RoI
includes ROOT/0/0/0, ROOT/0/0/1, ROOT/0/0/2, and ROOT/0/0/3, which
are all in the ROOT/0/0 quadkey. This is trivial to get.

    +---+---+---+   +---+---+---+
    |   |   |   |   |   |   |   |
    +---+=======+   +=======+---+
    |   ‖ v     ‖   ‖     v ‖   |
    +---‖       ‖   ‖       ‖---+
    |   ‖       ‖   ‖       ‖   |
    +---+=======+   +=======+---+

    +---+=======+   +=======+---+
    |   ‖       ‖   ‖       ‖   |
    +---‖       ‖   ‖       ‖---+
    |   ‖ v     ‖   ‖     v ‖   |
    +---+=======+   +=======+---+
    |   |   |   |   |   |   |   |
    +---+---+---+   +---+---+---+

In fact, it's not trivial to implement. Getting the super-quadkey right
above the vehicle is trivial, but identifying the 5 other ones is not.
It's much easier in fact to list the 8 surrounding quadkeys, and
optimise the list afterwards.

--------------------------------------------------------------------------------

Extending a zone consists in finding all the QuadKeys labelled '?'' below:

  ?   ?   ?   ?
    +---+---+
  ? | K | L | ?   ?   ?
    +---+---+---+---+
  ? |               | ?   ?
    |               +---+
  ? |               | M | ?   ?   ?   ?   ?
    |       Q       +---+---+-------+---+
  ? |               |       |       | N | ?
    |               |   P   |   O   +---+
  ? |               |       |       | ?   ?
    +---------------+-------+-------+
  ?   ?   ?   ?   ?   ?   ?   ?   ?   ?

In this example, Assuming Q is of depth D, we are looking to extend with
all QuadKeys that are of depth D+2

The simplest solution, is to iterate over all the QuadKeys in the zone,
namely: K, L, M, N, O, P, and Q, and find all the neighbouring QuadKeys
for each of them, and for each such neighbouring QuadKey, check if they
are covered by an existing QuadKey in the zone; if not, then it's an
actual neighbour of the zone, otherwise it is already in the zone.

When the requested depth is deeper (longer depth, smaller QuadKey) than
the depth of the considered QuadKey, the neighbour QuadKey of the same
depth is looked up, and then only the corresponding sub-QuadKeys are
returned. Consider the QuadKey Q below, and looking for its Western
neighbours that are at a depth d that is two-level deeper than Q itself:

    ┏━━━┯━━━┯━━━┯━━━┳━━━━━━━━━━━━━━━┓
    ┃   ┆   │   ┆ A ┃               ┃
    ┠┄┄┄┼┄┄┄┼┄┄┄┼┄┄┄┨               ┃
    ┃   ┆   │   ┆ B ┃               ┃
    ┠───┼───┼───┼───┨       Q       ┃
    ┃   ┆   │   ┆ C ┃               ┃
    ┠┄┄┄┼┄┄┄┼┄┄┄┼┄┄┄┨               ┃
    ┃   ┆   │   ┆ D ┃               ┃
    ┗━━━┷━━━┷━━━┷━━━┻━━━━━━━━━━━━━━━┛

This implies splitting the QuadKey West of Q twice, each time keeping
only the Eastern-most quadKeys. In practice, it means adding a tail
made of all combinations of {1,2} taken 2 at a time (i.e. 11, 12, 21,
and 22) and appending that to the quadkey sequence of that Western
QuadKey.

When the requested depth is shallower (shorter depth, bigger QuadKey)
than the considered QuadKey, the neighbour QuadKey of the same depth
is looked up, and it is made shallower to fit the requested depth. This
means that the returned QuadKey may encompass the considered QuadKey.
For example, consider the QuadKeys Q and R below, and looking for their
eight neighbours at a depth d that is two-level shallower than Q and R:

    ┏━━━┯━━━┯━━━┯━━━┓       ┏━━━┯━━━┯━━━┯━━━┳━━━┯━━━┯━━━┯━━━┓
    ┃   ┆   │   ┆   ┃       ┃   ┆   │   ┆   ┃   ┆   │   ┆   ┃
    ┠┄┄┄┼┄┄┄┼┄┄┄┼┄┄┄┨       ┠┄┄┄┼┄┄┄┼┄┄┄┼┄┄┄╂┄┄┄┼┄┄┄┼┄┄┄┼┄┄┄┨
    ┃   ┆ A │ B ┆ C ┃       ┃   ┆   │ I ┆ J ┃ K ┆   │   ┆   ┃
    ┠───┼───┼───┼───┨       ┠───┼───┼───┼───╂───┼───┼───┼───┨
    ┃   ┆ D │ Q ┆ E ┃       ┃   ┆   │ L ┆ R ┃ M ┆   │   ┆   ┃
    ┠┄┄┄┼┄┄┄┼┄┄┄┼┄┄┄┨       ┠┄┄┄┼┄┄┄┼┄┄┄┼┄┄┄╂┄┄┄┼┄┄┄┼┄┄┄┼┄┄┄┨
    ┃   ┆ F │ G ┆ H ┃       ┃   ┆   │ N ┆ O ┃ P ┆   │   ┆   ┃
    ┗━━━┷━━━┷━━━┷━━━┛       ┗━━━┷━━━┷━━━┷━━━┻━━━┷━━━┷━━━┷━━━┛
                   /                       /               /
                  X                       Y               Z

QuadKeys A through H are the 8 neighbours at the same depth as Q; when
made shallower, they will all map to the big QuadKey with bold borders
and labelled X.

What one may notice, is that requesting the South neighbour for Q, does
return a QuadKey that is more North than South of Q (similarly for East
vs. West) in such a circumstance.

However, that last point is not really an issue off itself:

  * if Q is standalone, then this big bold-bordered QuadKey is the
    appropriate one to represent all that is North/South/West/East of
    Q, and bordering Q. Anything smaller/bigger would not fit with the
    requested depth, and other QuadKeys further N/S/W/E would be too
    far away from Q to be meaningful.

  * if Q is not standalone, then it is bordered by at least one other
    QuadKey; that is one of the 3 intermediate QuadKeys, and thus the
    big, bold-bordered QuadKey will also fit as encomapssing the
    neighbourhood for those other intermediate QuadKeys.

QuadKeys I through P are the 8 neighbours at the same depth as R; when
made shallower, I, J, L, N, and O will all map to the big QuadKey with
bold border to the west, labelled Y, and that already contains R, while
K, M, and P will all map to the big QuadKey to the east, labelled Z,
and which is bordering R, and is the Eastern neighbour of Y.

--------------------------------------------------------------------------------

Adding two QuadZone together is relatively easy, once we have a way to
add QuadKeys to a QuadZone, and once we now how to optimise a QuadZone.
However, substracting a QuadZone from another one is relatively more
tricky.

The whole problem consist in finding QuadKeys of the two QuadZones, that
overlap each other, when there is no guarantee that they are of the same
depth.

The basic idea is to iterate over all the QuadKeys of the first QuadZone
and drop those that are in the other QuadZone. This removes the QuadKeys
that are entirely covered by the other QuadZone, but leaves those that
are only partially covered.

The second pass gets the intersection of the new QuadZone with the other
one. This gives the QuadKeys of the other QuadZone that still have to be
removed from the first.

Then, each QuadKey in the first QuadZone, that covers a QuadKey from the
intersection, is made shallower to the depth of the deepest QuadKey it
covers; such new shallower QuadKey that is not covered by the intersection
is then kept in the first QuadZone.
