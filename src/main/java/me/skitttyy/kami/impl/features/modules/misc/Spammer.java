package me.skitttyy.kami.impl.features.modules.misc;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Random;

public class Spammer extends Module {
    public static Spammer INSTANCE;

    public Spammer()
    {
        super("Spammer", Category.Misc);
        INSTANCE = this;
    }

    Timer spammerTimer = new Timer();

    public Value<String> mode = new ValueBuilder<String>()
            .withDescriptor("Mode")
            .withValue("File")
            .withModes("File", "Snapple")
            .register(this);
    Value<Number> delay = new ValueBuilder<Number>()
            .withDescriptor("Delay")
            .withValue(1d)
            .withRange(0.1d, 10d)
            .register(this);

    int spammerOffset = 0;

    public ArrayList<String> spammerText = new ArrayList<>();

    @SubscribeEvent
    public void onUpdate(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;


        boolean isFile = mode.getValue().equals("File");
        if (spammerText.isEmpty() && isFile)
        {
            ChatUtils.sendMessage(Formatting.RED + "Invalid spammer!");
            this.setEnabled(false);
            return;
        }
        if (spammerTimer.isPassed(delay.getValue().longValue() * 1000))
        {
            if (isFile)
            {
                if (spammerOffset >= spammerText.size())
                {
                    spammerOffset = 0;
                }
                if (spammerText.get(spammerOffset) == null) return;
            }

            ChatUtils.sendChatMessage(getSpammerText());
            spammerTimer.resetDelay();
        }
    }

    public String getSpammerText()
    {
        if (mode.getValue().equals("File"))
        {
            String text = spammerText.get(spammerOffset);
            spammerOffset++;
            return text;
        } else
        {
            return randomfact();
        }
    }

    @Override
    public String getHudInfo()
    {
        return mode.getValue() + "";
    }


    private static String facts = "2: Animals that lay eggs don't have belly buttons.?" +
            "3: Beavers can hold their breath for 45 minutes under water.?" +
            "4: Slugs have four noses.?" +
            "5: Camels have three eyelids.?" +
            "6: A honey bee can fly at 15mph.?" +
            "7: A queen bee can lay 800-1,500 eggs per day.?" +
            "8: A bee has five eyelids.?" +
            "9: The average speed of a housefly is 4.5 mph.?" +
            "10: Mosquitoes are attracted to people who just ate bananas.?" +
            "11: Flamingos turn pink from eating shrimp.?" +
            "12: Emus and kangaroos cannot walk backward.?" +
            "13: Cats have over 100 vocal cords.?" +
            "14: Camel's milk does not curdle.?" +
            "15: All porcupines float in water.?" +
            "16: The world's termites outweigh the world's humans about 10 to 1.?" +
            "17: A hummingbird weighs less than a penny.?" +
            "18: A jellyfish is approximately 95% water.?" +
            "19: Children tend to grow faster in the spring.?" +
            "20: Broccoli is the only vegetable that is also a flower.?" +
            "21: Peaches are members of the almond family.?" +
            "22: Alaska has the highest percentage of people who walk to work.?" +
            "23: The San Francisco cable cars are the only mobile national monument.?" +
            "26: The Hawaiian alphabet has only 12 letters.?" +
            "27: A ball of glass will bounce higher than a ball of rubber.?" +
            "29: On average a human being will spend up to 2 weeks kissing in his/her lifetime.?" +
            "30: Fish have eyelids.?" +
            "33: Termites eat through wood two times faster when listening to rock music!?" +
            "34: If you keep a goldfish in a dark room it will eventually turn white.?" +
            "37: A snail breathes through its foot.?" +
            "38: Fish cough.?" +
            "39: An ant's sense of smell is stronger than a dog's.?" +
            "40: It is possible to lead a cow up stairs but not down.?" +
            "42: Frogs cannot swallow with their eyes open.?" +
            "43: A cat's lower jaw cannot move sideways.?" +
            "44: The bullfrog is the only animal that never sleeps.?" +
            "45: Elephants are capable of swimming 20 miles per day.?" +
            "46: Elephants are the only mammals that can't jump.?" +
            "47: Giraffes have no vocal cords.?" +
            "48: Cats can hear ultrasound.?" +
            "49: Despite its hump, a camel has a straight spine.?" +
            "50: Mosquitoes have 47 teeth.?" +
            "51: There are 63,360 inches in a mile.?" +
            "52: About 11% of the people in the world are left-handed.?" +
            "54: The average smell weighs 760 nanograms.?" +
            "55: A human brain weighs about three pounds.?" +
            "56: 1/4 of the bones in your body are in your feet.?" +
            "57: You blink over 10,000,000 times a year.?" +
            "58: A sneeze travels out of your mouth at over 100 miles an hour.?" +
            "59: Brain waves can be used to power an electric train.?" +
            "60: The tongue is the fastest healing part of the body.?" +
            "61: Pigs can get sunburn.?" +
            "62: The life span of a taste bud is about ten days.?" +
            "63: The average human produces 10,000 gallons of saliva in a lifetime.?" +
            "64: Strawberries contain more vitamin C than oranges.?" +
            "65: A one-day weather forecast requires about 10 billion math calculations.?" +
            "66: Americans, on average, eat 18 acres of pizza a day.?" +
            "67: There are 18 different animal shapes in the animal cracker zoo.?" +
            "68: The longest one-syllable word is \"screeched.\"?" +
            "69: No word in the English language rhymes with month.?" +
            "71: There is a town called \"Big Ugly\" in West Virginia.?" +
            "73: The average person spends 2 weeks over his/her lifetime waiting for a traffic light to change.?" +
            "74: You share your birthday with at least 9 million other people in the world.?" +
            "77: No piece of paper can be folded more than seven times.?" +
            "78: Alaska is the most eastern and western state in the U.S.?" +
            "79: There are 119 grooves on the edge of a quarter.?" +
            "80: About 18 percent of animal owners share their bed with their pet.?" +
            "81: Alaska has more caribou than people.?" +
            "83: Googol is a number (1 followed by 100 zeros).?" +
            "84: Oysters can change from one gender to another and back again.?" +
            "85: The Mona Lisa has no eyebrows.?" +
            "86: Until the 19th century, solid blocks of tea were used as money in Siberia.?" +
            "87: A mile on the ocean and a mile on land are not the same distance.?" +
            "88: A ten gallon hat holds less than one gallon of liquid.?" +
            "90: The average raindrop falls at seven mph.?" +
            "92: Fish can drown.?" +
            "93: A kangaroo can jump 30 feet.?" +
            "94: Lizards communicate by doing push-ups.?" +
            "95: Squids can have eyes the size of a volleyball.?" +
            "96: The average American will eat 35,000 cookies in his/her lifetime.?" +
            "97: A turkey can run at 20 mph.?" +
            "98: When the moon is directly overhead, you weigh slightly less.?" +
            "99: You burn about 20 calories per hour chewing gum.?" +
            "102: A one-minute kiss burns about 26 calories.?" +
            "106: You would weigh less on the top of a mountain than at sea level.?" +
            "107: You burn more calories sleeping than you do watching TV.?" +
            "109: Smelling apples and/or bananas can help you lose weight.?" +
            "110: Frogs never drink.?" +
            "111: Only male turkeys gobble.?" +
            "112: At birth, a Dalmatian is always pure white.?" +
            "116: The largest fish is the whale shark – it can be over 50 feet long and weigh two tons.?" +
            "118: Honeybees are the only insects that create a form of food for humans.?" +
            "119: The hummingbird is the only bird that can fly backwards.?" +
            "120: The only continent without reptiles or snakes is Antarctica.?" +
            "121: The only bird that can swim and not fly is a penguin.?" +
            "122: A duck can't walk without bobbing its head.?" +
            "123: Beavers were once the size of bears.?" +
            "124: Seals sleep only one and a half minutes at a time.?" +
            "125: Pigeons have been trained by the U.S. Coast Guard to spot people lost at sea.?" +
            "126: A pigeon's feathers are heavier than its bones.?" +
            "127: A hummingbird's heart beats 1,400 times a minute.?" +
            "128: Dragonflies have six legs but cannot walk.?" +
            "130: Koalas and humans are the only animals with unique fingerprints.?" +
            "131: Penguins have an organ above their eyes that converts seawater to fresh water.?" +
            "132: A crocodile cannot move its tongue.?" +
            "133: Honeybees navigate by using the sun as a compass.?" +
            "136: Strawberries are the only fruits whose seeds grow on the outside.?" +
            "137: The city of Los Angeles has about 3x more automobiles than people.?" +
            "138: Hawaii is the only U.S. state that grows coffee commercially.?" +
            "139: Hawaii is the only state with one school district.?" +
            "141: The square dance is the official dance of the state of Washington.?" +
            "142: Hawaii is the only U.S. state never to report a temperature of zero degrees F or below.?" +
            "143: \"Q\" is the only letter in the alphabet not appearing in the name of any U.S. state.?" +
            "144: Texas is the only state that permits residents to cast absentee ballots from space.?" +
            "145: Lake Superior is the world's largest lake.?" +
            "146: The smallest county in America is New York County, better known as Manhattan.?" +
            "147: Panama is the only place in the world where you can see the sun rise on the Pacific and set on the Atlantic.?" +
            "148: The tallest man was 8 ft. 11 in.?" +
            "149: Theodore Roosevelt was the only president who was blind in one eye.?" +
            "150: The first sport to be filmed was boxing in 1894.?" +
            "153: The speed limit in NYC was eight mph in 1895.?" +
            "155: In 1926, the first outdoor mini-golf courses were built on rooftops in NYC.?" +
            "156: Swimming pools in the U.S. contain enough water to cover San Francisco.?" +
            "158: The first MTV video was \"Video Killed the Radio Star\" by the Buggles.?" +
            "159: The first TV show to ever be put into reruns was \"The Lone Ranger.\"?" +
            "160: One alternate title that had been considered for NBC's hit \"Friends\" was \"Insomnia Café.\"?" +
            "162: The temperature of the sun can reach up to 15 million degrees Fahrenheit.?" +
            "163: The first penny had the motto \"Mind your own business.\"?" +
            "164: The first vacuum was so large, it was brought to a house by horses.?" +
            "165: Your eye expands up to 45% when looking at something pleasing.?" +
            "166: Before mercury, brandy was used to fill thermometers.?" +
            "167: You'd have to play ping-pong for about 12 hours to lose one pound.?" +
            "168: One brow wrinkle is the result of 200,000 frowns.?" +
            "169: The first human-made object to break the sound barrier was a whip.?" +
            "170: In 1878, the first telephone book ever issued contained only 50 names.?" +
            "171: The most sensitive parts of the body are the mouth and the fingertips.?" +
            "172: The eye makes movements 50 times every second.?" +
            "174: The world's biggest pyramid is not in Egypt, but in Mexico.?" +
            "175: In 1634, tulip bulbs were a form of currency in Holland.?" +
            "176: The first bike was called a hobbyhorse.?" +
            "177: The first sailing boats were built in Egypt.?" +
            "178: The first ballpoint pens were sold in 1945 for $12.00.?" +
            "180: The first VCR was made in 1956 and was the size of a piano.?" +
            "182: A rainbow can only be seen in the morning or late afternoon.?" +
            "183: The Capitol building in Washington, D.C. has 365 steps to represent every day of the year.?" +
            "184: The most used letters in the English language are E, T, A, O, I and N.?" +
            "185: A male kangaroo is called a boomer.?" +
            "186: A female kangaroo is called a flyer.?" +
            "188: Antarctica is the driest, coldest, windiest, and highest continent on earth.?" +
            "189: The Sahara Desert stretches farther than the distance from California to New York.?" +
            "190: Thailand means \"Land of the Free.\"?" +
            "191: Popcorn was invented by the American Indians.?" +
            "192: Jupiter spins so fast that there is a new sunrise nearly every 10 hours.?" +
            "193: The year that read the same upside down was 1961. That won't happen again until 6009.?" +
            "194: You don't have to be a lawyer to be a Supreme Court Justice.?" +
            "195: Eleven of the 50 U.S. states are named after an actual person.?" +
            "196: If you doubled one penny every day for 30 days, you would have $5,368,709.?" +
            "197: The first person crossed Niagara Falls by tightrope in 1859.?" +
            "198: The U.S. is the largest country named after a real person (Amerigo Vespucci).?" +
            "201: The only one-syllabled U.S. state is Maine.?" +
            "203: Atlantic salmon are capable of leaping 15 feet high.?" +
            "206: Over 1 million Earths would fit inside the Sun.?" +
            "207: Before 1687 clocks were made with only an hour hand.?" +
            "208: Add up opposing sides of a dice cue and you'll always get seven.?" +
            "211: The average koala sleeps 22 hours each day.?" +
            "212: Galapagos turtles can take up to three weeks to digest a meal.?" +
            "215: Tennessee banned the use of a lasso to catch fish.?" +
            "217: Blackboard chalk contains no chalk.?" +
            "218: A jackrabbit can travel more than 12 feet in one hop.?" +
            "219: An electric eel can release a charge powerful enough to start 50 cars.?" +
            "220: Porcupines each have 30,000 quills.?" +
            "221: The game of basketball was first played using a soccer ball and two peach baskets.?" +
            "224: America's 1st roller coaster was built in 1827 to carry coal from a mine to boats below.?" +
            "228: There is a town in South Dakota named Tea.?" +
            "229: The Caspian Sea is actually a lake.?" +
            "230: Caterpillars have over 2,000 muscles.?" +
            "232: The blue whale's heart is the size of a small car.?" +
            "233: There are seven letters that look the same upside down as right side up.?" +
            "235: The biggest pig in recorded history weighed almost one ton.?" +
            "236: Cows give more milk when they listen to music.?" +
            "237: The number of times a cricket chirps in 15 seconds, plus 37, will give you the current air temperature.?" +
            "238: An ostrich's brain is smaller than its eye.?" +
            "240: \"Challenger Deep\" is the deepest point on Earth and can hold 25 Empire State Buildings end to end.?" +
            "241: The only cactus plantation in the world is in Mississippi.?" +
            "243: If you put all the streets in New York City in a straight line, they would stretch to Japan.?" +
            "244: The watermelon seed-spitting world record is about 70 feet.?" +
            "245: The first typewriter was called the \"literary piano.\"?" +
            "246: The \"silk\" of a spider is stronger than steel threads of the same diameter.?" +
            "248: Snoopy is the most common dog name beginning with the letter S.?" +
            "249: The 1st public message to be transmitted via Morse code was \"A patient waiter is no loser.\"?" +
            "250: Mongolians invented lemonade around 1299 A.D.?" +
            "251: There are more French restaurants in New York City than in Paris.?" +
            "253: The first TV remote control, introduced in 1950, was called Lazy Bones.?" +
            "254: The only bird who can see the color blue is the owl.?" +
            "255: Among North Atlantic lobsters, about 1 in 5,000 is born bright blue.?" +
            "256: There are more saunas than cars in Finland.?" +
            "257: The first food eaten in space by a U.S. astronaut was applesauce.?" +
            "259: The original recipe for chocolate contained chili powder instead of sugar.?" +
            "260: Underwater hockey is played with a 3-pound puck.?" +
            "263: Bowling pins need to tip over a mere 7 1/2 degrees to fall down.?" +
            "264: Your breathing rate increases when you start to type.?" +
            "265: About 90% of all garlic consumed in the U.S. comes from Gilroy, CA.?" +
            "267: Double Dutch jump rope is considered a cross-training sport.?" +
            "268: One lemon tree will produce about 1,500 lemons a year.?" +
            "269: Horseback riding can improve your posture.?" +
            "270: Colors like red, yellow and orange make you hungry.?" +
            "271: Dim lights reduce your appetite.?" +
            "272: At birth a human has 350 bones, but only 206 bones when full grown.?" +
            "273: Each year, the average American eats about 15 pounds of apples.?" +
            "275: It took the first man to walk around the world four years, three months and 16 days to complete his journey.?" +
            "276: Grizzly bears run as fast as the average horse.?" +
            "278: China only has one time zone.?" +
            "279: Canada has the longest coastline of any country in the world.?" +
            "280: The amount of concrete used in the Hoover Dam could build a highway from New York to California.?" +
            "281: The original name of Nashville, Tennessee was Big Salt Lick.?" +
            "282: If you drive from Los Angeles to Reno, NV, you will be heading west.?" +
            "283: A compass needle does not point directly north.?" +
            "284: Mt. Everest has grown one foot over the last 100 years.?" +
            "285: In ancient Rome, lemons were used as an antidote to all poisons.?" +
            "286: The height of the Eiffel Tower varies by as much as 6 inches depending on the temperature.?" +
            "287: Wisconsin has points located farther east than parts of Florida.?" +
            "288: Four Corners, AZ, is the only place where a person can stand in 4 states at the same time.?" +
            "291: Africa is divided into more countries than any other continent.?" +
            "292: Heavier, not bigger lemons, produce more juice.?" +
            "293: Vermont is the only New England state without a seacoast.?" +
            "294: No only child has been a U.S. President.?" +
            "295: Leonardo da Vinci could draw with one hand while writing with the other.?" +
            "296: In 1860, Abraham Lincoln grew a beard at the suggestion of an 11-year-old girl.?" +
            "297: David Rice Atchison was President of the United States for only one day.?" +
            "298: The sail fish has been clocked at speeds of over 60 miles per hour.?" +
            "299: The Library of Congress has over 600 miles of shelves.?" +
            "300: Pennsylvania is misspelled on the Liberty Bell, because that is how they spelled it in the 18th century.?" +
            "301: William Shakespeare was born and died on the same day:  April 23.?" +
            "302: Ketchup was once sold as a medicine.?" +
            "303: Napoleon suffered from a fear of cats.?" +
            "304: In 1900, 1/3 of all automobiles in New York City were powered by electricity.?" +
            "305: The 4th Earl of Sandwich invented the sandwich so he could eat and gamble at the same time.?" +
            "306: In the Middle Ages, chicken soup was considered an aphrodisiac.?" +
            "309: Ancient Egyptians believed the \"vein of love\" ran from the third finger on the left hand to the heart.?" +
            "310: The word \"facetious\" features all the vowels in alphabetical order.?" +
            "311: The standard Chinese typewriter has 1,500 characters.?" +
            "312: A flea can jump 30,000 times without stopping.?" +
            "313: \"O\" is the oldest letter of the alphabet, dating back to 3000 B.C.?" +
            "314: The Japanese word \"judo\" means \"the gentle way.\"?" +
            "315: No two lip impressions are the same.?" +
            "316: It took Leonardo da Vinci 12 years to paint the lips of Mona Lisa.?" +
            "318: Top-performing companies are called \"blue chips\" after the costliest chips in casinos.?" +
            "319: The name for the space between your eyebrows is \"nasion.\"?" +
            "321: The word \"purple\" does not rhyme with any other word in the English language.?" +
            "324: The legs of bats are too weak to support their weight, so they hang upside down.?" +
            "325: 75% of people wash from top to bottom in the shower.?" +
            "326: On average, you'll spend a year of your life looking for misplaced objects.?" +
            "327: Chewing gum was invented in New York City in 1870 by Thomas Adams.?" +
            "328: The Statue of Liberty features 7 points in her crown- one for each of the continents.?" +
            "329: The world's first escalator was built in Coney Island, NY, in 1896.?" +
            "330: The top of the Empire State Building was originally built as a place to anchor blimps.?" +
            "331: The area code in Cape Canaveral, Fl, is 321.?" +
            "332: Ohio is the only U.S. state that does not have a rectangular flag.?" +
            "333: Long Island is the largest island in the Continental U.S.?" +
            "335: Maine produces more toothpicks than any other state in the U.S.?" +
            "336: The last letter to be added to our alphabet was J.?" +
            "339: There are more doughnut shops per capita in Canada than in any other country.?" +
            "340: There is an underground mushroom in Oregon that measures 3.5 miles across.?" +
            "341: Of the 92 counties in Indiana, only 5 observe daylight savings time.?" +
            "342: California and Arizona grow approximately 95% of the fresh lemons in the U.S.?" +
            "343: The term 007 was derived from 20007, the home zip code of many Washington, D.C. agents.?" +
            "344: Leonardo da Vinci discovered that a tree's rings reveal its age.?" +
            "345: The popsicle was invented in 1905 by an 11-year-old boy.?" +
            "346: The medical term for writer's cramp is graphospasm.?" +
            "347: A male firefly's light is twice as bright as a female's.?" +
            "348: It is estimated that the world's oceans contain 10 billion tons of gold.?" +
            "351: Cold water weighs less than hot water.?" +
            "352: Storm clouds hold about 6 trillion raindrops.?" +
            "353: The weight of the moon is 81 billion tons.?" +
            "354: Bamboo can grow three feet in one day.?" +
            "355: A tune that gets stuck in your head is called an earworm.?" +
            "356: You exhale air at 15 m.p.h.?" +
            "357: A baboon is a variety of lemon.?" +
            "358: Butterflies were formerly known by the name Flutterby.?" +
            "360: Mexican jumping beans jump to get out of sunlight.?" +
            "362: \"Arachibutlphobia\" is the fear of peanut butter sticking to the roof of your mouth.?" +
            "363: Pearls dissolve in vinegar.?" +
            "364: Borborygmi is the noise that your stomach makes when you are hungry.?" +
            "366: The center of some golf balls contain honey.?" +
            "367: International tug of war rules state that the rope must be over 100-feet long.?" +
            "368: In 2003, a 6-year-old from Naples, FL was ticketed for not having a permit for her lemonade stand.?" +
            "369: On Valentine's Day, there is no charge to get married in the Empire State Building's chapel.?" +
            "370: Heat, not sunlight, ripens tomatoes.?" +
            "372: A housefly hums in the key of F.?" +
            "373: Endocarp is the edible pulp inside a lemon.?" +
            "374: Thomas Edison coined the word \"hello\" and introduced it as a way to answer the phone.?" +
            "375: \"Way\" is the most frequently used noun in the English language.?" +
            "376: The \"high five\" was introduced by a professional baseball player in 1977.?" +
            "377: \"Disco\" means \"I learn\" in Latin.?" +
            "378: It costs the U.S. government 2.5 cents to produce a quarter.?" +
            "381: Baboons were once trained by Egyptians to wait on tables.?" +
            "382: The official state gem of Washington is petrified wood.?" +
            "383: Mount Katahdin in Maine is the first place in the U.S. to get sunlight each morning.?" +
            "384: Each year, the average person walks the distance from NY to Miami.?" +
            "386: New York City's public school students represent about 188 different countries.?" +
            "389: In the U.S., all interstate highways that run east to west are even-numbered.?" +
            "392: Three out of every six Americans live within fifty miles of where they were born.?" +
            "393: The raised bump reflectors on U.S. roads are named \"Botts dots.\"?" +
            "394: Nearly 9,000 people injure themselves with a toothpick each year.?" +
            "395: It is impossible to sneeze with your eyes open.?" +
            "396: The dragonfly can reach speeds of up to 36 mph.?" +
            "398: Hippos can open their mouths 180 degrees.?" +
            "402: Christopher Columbus brought the first lemon seeds to America.?" +
            "405: The East Antarctic Ice Sheet is as thick as the Alps Mountains are high.?" +
            "406: The deepest place in the ocean is about seven miles deep.?" +
            "408: Panda bears eat up to 16 hours a day.?" +
            "410: Bald eagles can swim using a stroke similar to the butterfly stroke.?" +
            "411: Lifejackets used to be filled with sunflower seeds for flotation.?" +
            "412: Two trees can create enough oxygen for a family of four.?" +
            "413: The T-rex’s closest living relative is the chicken.?" +
            "414: Chameleons can move both their eyes in different directions at the same time.?" +
            "416: Many butterflies and moths are able to taste with their feet.?" +
            "419: A jiffy is an actual time measurement equaling 1/100th of a second.?" +
            "421: Greyhounds can reach speeds of 45 miles per hour.?" +
            "422: Apples, peaches and raspberries are all members of the rose family.?" +
            "423: U.S. paper currency isn’t made of paper – it’s actually a blend of cotton and linen.?" +
            "424: The ZIP in the ZIP code stands for Zone Improvement Plan.?" +
            "425: Kangaroos can’t walk backwards.?" +
            "426: The Empire State Building has 73 elevators.?" +
            "427: Lemons ripen after you pick them, but oranges do not.?" +
            "428: There are 118 ridges on the edge of a United States dime.?" +
            "429: There are 336 dimples on a regulation American golf ball.?" +
            "430: One acre of peanuts will make about 30,000 peanut butter sandwiches.?" +
            "431: A twit is the technical term for a pregnant goldfish.?" +
            "434: In the U.S. a pig has to weigh more than 180 lbs to be called a hog.?" +
            "435: Bloodhounds can track a man by smell for up to 100 miles.?" +
            "436: Beavers have orange teeth.?" +
            "437: The woodpecker can hammer wood up to 16 times per second.?" +
            "438: Mount Everest rises a few millimeters every year.?" +
            "439: Snails can sleep for up to three years.?" +
            "440: The pupils in goats’ eyes are rectangular.?" +
            "441: Jousting is the official sport in the state of Maryland.?" +
            "442: Bees’ wings beat about 11,400 times per minute.?" +
            "443: The pound sign, or #, is called an octothorp.?" +
            "444: The Statue of Liberty wears a size 879 sandal.?" +
            "445: If there are two full moons in a month, the second one is called a blue moon.?" +
            "446: You breathe in about 13 pints of air every minute.?" +
            "449: Sound travels quicker in water than in air.?" +
            "450: A group of cats is called a clowder.?" +
            "451: Human eyes have over two million working parts.?" +
            "452: There are approximately 9,000 taste buds on your tongue.?" +
            "453: Raindrops can fall as fast as 20 miles per hour.?" +
            "454: Polar bear fur is transparent, not white.?" +
            "455: Lobsters can live up to 50 years.?" +
            "457: The first traffic light was in use in London in 1868, before the advent of cars.?" +
            "458: Fresh cranberries can be bounced like a rubber ball.?" +
            "459: A group of a dozen or more cows is called a ‘flink.’?" +
            "460: Astronauts actually get taller when in space.?" +
            "461: A fifteen-year-old boy invented earmuffs in 1873.?" +
            "462: There is a ranch in Texas that is bigger than the entire state of Rhode Island.?" +
            "463: The dot over the letter i is called a tittle.?" +
            "465: The great white shark can go up to three months between meals.?" +
            "466: During the Boston Tea Party, 342 chests of tea were thrown into the harbor.?" +
            "467: Pluto takes 248 years to orbit the sun once.?" +
            "468: Camels have three eyelids.?" +
            "469: 454 U.S. dollar bills weigh exactly one pound.?" +
            "470: Dairy cows drink up to 50 gallons of water per day.?" +
            "472: A nautical mile is 800 feet longer than a land mile.?" +
            "473: Antarctica has as much ice as the Atlantic Ocean has water.?" +
            "478: Candles will burn longer and drip less if they are placed in the freezer a few hours before using.?" +
            "481: Over 50 percent of your body heat is lost through your head and neck.?" +
            "483: Smile more – every two thousand frowns creates one wrinkle.?" +
            "654: New York taxi drivers collectively speak about 60 languages.?" +
            "658: New York City is made up of 50 islands.?" +
            "660: The strike note of The Liberty Bell is E flat.?" +
            "661: Pigs were banished from Philadelphia's city streets in 1710.?" +
            "663: About 40% of America's population lives within a one day drive to Philadelphia.?" +
            "664: It is against the law to put pretzels in bags in Philadelphia.?" +
            "669: In the game Monopoly, the properties are named after streets in Atlantic City.?" +
            "674: The oldest living animal ever found was a 405 year-old clam, named Ming by researchers.?" +
            "675: More than 180 countries celebrate Earth Day together every April 22nd.?" +
            "676: At 5 feet, the whooping crane is the tallest bird in North America.?" +
            "677: A full-grown tree produces enough oxygen to support a family of four.?" +
            "678: Unlike your housecat, the Siberian tiger actually loves to swim!?" +
            "679: A tiger’s night vision is six times better than a human’s.?" +
            "680: More Siberian tigers live in zoos than in the wild.?" +
            "681: The jaguar, the largest cat in the Western Hemisphere, once lived all over the southern U.S.?" +
            "682: The giant panda can eat up to 83 lbs of bamboo a day.?" +
            "683: Wildlife Forever has helped plant more than 132,000 trees in America since its founding in 1987.?" +
            "684: Manhattan Island was once home to as many different species as Yellowstone National Park.?" +
            "685: Dogs can make about 10 sounds, while cats make about 100.?" +
            "686: A Pelican can hold more food in its beak than its belly.?" +
            "687: The average cat can jump 5 times as high as its tail is long.?" +
            "688: Flying fish leap out of the water at 20 mph or more, and can glide for over 500 feet.?" +
            "689: The roadrunner chases after its prey at a blurring speed of up to 25 mph.?" +
            "691: The archer fish can spit water up to 7 feet to shoot down bugs from overhanging leaves.?" +
            "692: The spotted skunk does a handstand to warn off its enemies before it sprays its stench.?" +
            "693: A male cricket's ear is located on the tibia of its leg.?" +
            "694: Spiny lobsters migrate in groups of 50 or more, forming a conga line on the ocean floor.?" +
            "695: The National Park Service manages over 350 parks on 80 million acres of public land.?";

    private static String facts2 =

            "698: Stepping out for a walk every day can actually help you sleep better at night.?" +
                    "704: Recycled paper is made using 40% less energy than normal paper.?" +
                    "705: Every ton of recycled paper saves about 17 trees.?" +
                    "706: Steel is 100% recyclable.?" +
                    "707: Most rechargeable batteries can be recharged up to 1,000 times.?" +
                    "713: An egg that is fresh will sink in water, but a stale one won't.?" +
                    "714: A camel can drink 25 gallons of water in less than three minutes.?" +
                    "715: In one day, a full-grown oak tree expels 7 tons of water through its leaves.?" +
                    "716: There is a museum of strawberries in Belgium.?" +
                    "717: Mangoes are the most-consumed fruit in the world.?" +
                    "718: Strawberries have an average of 200 seeds.?" +
                    "719: A strawberry is not an actual berry, but a banana is.?" +
                    "720: Fresh apples float because 25 percent of their volume is air.?" +
                    "722: The peach was the first fruit to be eaten on the moon.?" +
                    "724: A pineapple is neither an apple or a pine. It is, in fact, a large berry.?" +
                    "725: Only female mosquitoes bite.?" +
                    "726: A polar bear cannot be seen by an infrared camera, due to its transparent fur.?" +
                    "727: A spider’s silk is stronger than steel.?" +
                    "728: The planet Saturn's density is lower than water; in fact, it would float if placed in water.?" +
                    "729: Twins have a very high occurrence of left handedness.?" +
                    "730: The fear of vegetables is called lachanophobia.?" +
                    "731: There are over 2,000 different species of cactuses.?" +
                    "732: The chicken is the closest living relative of Tyrannosaurus Rex.?" +
                    "733: All scorpions glow.?" +
                    "734: Potatoes have more chromosomes than humans.?" +
                    "735: A full moon is nine times brighter than a half moon.?" +
                    "737: The human brain takes up 2% of human body weight but uses 20% of its energy.?" +
                    "740: Poison Ivy is not Ivy and Poison Oak is not an Oak. They are both part of the Cashew family.?" +
                    "741: Plants, like humans, can run a fever if they are sick.?" +
                    "742: Over half of the world's geysers are found in Yellowstone National Park.?" +
                    "743: A group of geese on the ground is a gaggle, a group of geese in the air is a skein.?" +
                    "744: Polar bears can smell a seal from 20 miles away.?" +
                    "745: Armadillos have four babies at a time and they are always all the same sex.?" +
                    "746: The only insect that can turn its head is a praying mantis.?" +
                    "747: Alaska was bought from Russia for about 2 cents an acre.?" +
                    "748: A dog's average body temperature is 101 degrees Fahrenheit.?" +
                    "750: The common garden worm has five pairs of hearts.?" +
                    "751: Flamingos can only eat with their heads upside down.?" +
                    "752: A group of twelve or more cows is called a flink.?" +
                    "753: A group of goats is called a trip.?" +
                    "754: An alligator can go through 3,000 teeth in a lifetime.?" +
                    "755: There are more chickens than people in the world.?" +
                    "756: Penguins can jump 6 feet.?" +
                    "757: There are approximately 7,000 feathers on an eagle.?" +
                    "758: The only lizard that has a voice is the Gecko.?" +
                    "759: A rhinoceros horn is made of compacted hair.?" +
                    "760: Dolphins sleep with one eye open.?" +
                    "762: A duck has three eyelids.?" +
                    "763: Tigers have striped skin, not just striped fur.?" +
                    "764: The muzzle of a lion is like a fingerprint - no two lions have the same pattern of whiskers.?" +
                    "766: The hippopotamus has the capability to remain underwater for as long as five minutes.?" +
                    "768: Most elephants weigh less than the tongue of a blue whale.?" +
                    "769: If a sheep and a goat mate the offspring is called a geep.?" +
                    "770: Pistol shrimp can make a noise loud enough to break glass.?" +
                    "773: Mountain goats aren't actually goats. They are antelopes.?" +
                    "774: Koalas only drink water in extreme heat or drought.?" +
                    "775: Bees are born fully grown.?" +
                    "776: Ferret comes from the Latin word for little thief.?" +
                    "777: Cats have 2 sets of vocal cords:  one for purring and one for meowing.?" +
                    "778: Some bears build nests in trees to sunbathe and rest.?" +
                    "779: A group of jellyfish is called a smack.?" +
                    "780: The indentation between the nose and the upper lip is called the philtrum.?" +
                    "782: The human jaw can generate a force up to 200 pounds on the molars.?" +
                    "784: The human brain is about 80% water.?" +
                    "785: The middle finger has the fastest growing nail.?" +
                    "786: The brain operates on the same amount of power as a 10-watt light bulb.?" +
                    "787: Like fingerprints, everyone's tongue print is different.?" +
                    "790: Your big toe only has 2 bones and the rest have 3.?" +
                    "791: The average person takes 23,000 breaths a day.?" +
                    "797: It is illegal to run out of gas in Youngstown, Ohio.?" +
                    "798: Tennessee was previously named Franklin after Benjamin Franklin.?" +
                    "799: The official color of California's Golden Gate Bridge is International Orange.?" +
                    "801: It is not possible to tickle yourself.?" +
                    "803: Antarctica is the only continent with no owls.?" +
                    "804: There are 293 ways to make change for a dollar.?" +
                    "807: Shakespeare invented the word assassination and bump.?" +
                    "809: French author Michel Thayer published a 233 page novel which has no verbs.?" +
                    "810: Australia is the only continent without an active volcano.?" +
                    "811: The dots on a domino are called pips.?" +
                    "812: 111,111,111 x 111,111,111 = 12,345,678,987,654,321?" +
                    "814: Tug-of-war was an Olympic sport in the early 1900's.?" +
                    "815: The name of the city we call Bangkok is 115 letters long in the Thai language.?" +
                    "816: In Ancient Greece, throwing an apple to a woman was considered a marriage proposal.?" +
                    "817: Karate originated in India.?" +
                    "818: The infinity sign is called a lemniscate.?" +
                    "819: Children grow faster during springtime.?" +
                    "821: It takes an interaction of 72 muscles to produce human speech.?" +
                    "823: Sailors once thought wearing gold earrings improved eyesight.?" +
                    "825: Our eyes are always the same size from birth, but our nose and ears never stop growing.?" +
                    "827: Your skull is made up of 29 different bones.?" +
                    "828: Every hour more than one billion cells in the body must be replaced.?" +
                    "829: Women's hearts typically beat faster than men's hearts.?" +
                    "831: Adults laugh only about 15 to 100 times a day, while six-year-olds laugh an average of 300 times a day.?" +
                    "833: Children have more taste buds than adults.?" +
                    "834: Right handed people tend to chew food on the right side and lefties chew on the left.?" +
                    "839: A cucumber consists of 96% water.?" +
                    "842: Vanilla is used to make chocolate.?" +
                    "844: One lump of sugar is equivalent to three feet of sugar cane.?" +
                    "845: A lemon contains more sugar than a strawberry.?" +
                    "847: Until the nineteenth century, solid blocks of tea were used as money in Siberia.?" +
                    "848: Wild camels once roamed Arizona's deserts.?" +
                    "849: New York was the first state to require cars to have license plates.?" +
                    "851: Miami installed the first ATM for rollerbladers.?" +
                    "853: Hawaii has its own time zone.?" +
                    "855: Oregon has more ghost towns than any other US state.?" +
                    "860: Cleveland, OH is home to the first electric traffic lights.?" +
                    "861: South Carolina is home to the first tea farm in the U.S.?" +
                    "863: The term rookies comes from a Civil War term, reckie, which was short for recruit.?" +
                    "864: Taft was the heaviest U.S. President at 329lbs; Madison was the smallest at 100lbs.?" +
                    "865: Harry Truman was the last U.S. President to not have a college degree.?" +
                    "866: Abraham Lincoln was the tallest U.S. President at 6'4\", while James Madison was the shortest at 5'4\".?" +
                    "867: Franklin Roosevelt was related to 5 U.S. Presidents by blood and 6 by marriage.?" +
                    "868: Thomas Jefferson invented the coat hanger.?" +
                    "869: Theodore Roosevelt had a pet bear while in office.?" +
                    "870: President Warren G. Harding once lost white house china in a poker game.?" +
                    "871: Ulysses Simpson Grant was fined $20.00 for speeding on his horse.?" +
                    "872: President William Taft weighed over 300 lbs and once got stuck in the white house bathtub.?" +
                    "873: President William McKinley had a pet parrot that he named \"Washington Post.\"?" +
                    "874: Harry S. Truman's middle name is S.?" +
                    "876: The youngest U.S. president to be in office was Theodore Roosevelt at age 42.?" +
                    "879: Most Koala bears can sleep up to 22 hours a day.?" +
                    "882: In 1859, 24 rabbits were released in Australia. Within 6 years, the population grew to 2 million.?" +
                    "883: Butterflies can taste with their hind feet.?" +
                    "884: A strand from the web of a golden spider is as strong as a steel wire of the same size.?" +
                    "886: The bumblebee bat is one of the smallest mammals on Earth. It weighs less than a penny.?" +
                    "887: The Valley of Square Trees in Panama is the only known place in the world where trees have rectangular trunks.?" +
                    "889: The original Cinderella was Egyptian and wore fur slippers.?" +
                    "899: The plastic things on the end of shoelaces are called aglets.?" +
                    "900: Neckties were first worn in Croatia, which is why they were called cravats.?" +
                    "902: Barbie's full name is Barbara Millicent Roberts.?" +
                    "903: The first TV toy commercial aired in 1946 for Mr. Potato Head.?" +
                    "904: If done perfectly, any Rubik's Cube combination can be solved in 17 turns.?" +
                    "905: The side of a hammer is called a cheek.?" +
                    "908: In Athens, Greece, a driver's license can be taken away by law if the driver is deemed either unbathed or poorly dressed.?" +
                    "909: In Texas, it is illegal to graffiti someone's cow.?" +
                    "910: Less than 3% of the water on Earth is fresh.?" +
                    "911: A cubic mile of fog is made up of less than a gallon of water.?" +
                    "913: The Saturn V moon rocket consumed 15 tons of fuel per second.?" +
                    "914: A manned rocket can reach the moon in less time than it took a stagecoach to travel the length of England.?" +
                    "915: At room temperature, the average air molecule travels at the speed of a rifle bullet.?" +
                    "919: The lollipop was named after one of the most famous Racehorses in the early 1900s, Lolly Pop.?" +
                    "920: Buzz Aldrin was one of the first men on the moon. His mother's maiden name was also Moon.?" +
                    "922: Maine is the only state with a one-syllable name.?" +
                    "924: The highest denomination issued by the U.S. was the 100,000 dollar bill.?" +
                    "925: The White House was originally called the President's Palace. It became The White House in 1901.?" +
                    "926: George Washington was the only unanimously elected President.?" +
                    "927: John Adams was the only President to be defeated by his Vice President, Thomas Jefferson.?" +
                    "928: New York City has over 800 miles of subway track.?" +
                    "929: Manatees' eyes close in a circular motion, much like the aperture of a camera.?" +
                    "930: Even though it is nearly twice as far away from the Sun as Mercury, Venus is by far the hottest planet.?" +
                    "931: The nothingness of a black hole generates a sound in the key of B flat.?" +
                    "932: Horses can't vomit.?" +
                    "934: Babies are born with about 300 separate bones, but adults have 206.?" +
                    "935: Newborn babies cannot cry tears for at least three weeks.?" +
                    "936: A day on Venus lasts longer than a year on Venus.?" +
                    "937: Squirrels lose more than half of the nuts they hide.?" +
                    "939: The penny was the first U.S. coin to feature the likeness of an actual person.?" +
                    "940: Forty percent of twins invent their own language.?" +
                    "941: In South Korea, it is against the rules for a professional baseball player to wear cabbage leaves inside of his hat.?" +
                    "942: Curly hair follicles are oval, while straight hair follicles are round.?" +
                    "943: George Washington had false teeth made of gold, ivory, and lead - but never wood.?" +
                    "944: Napoleon Bonaparte was actually not short. At 5' 7\", he was average height for his time.?" +
                    "945: The Inca built the largest and wealthiest empire in South America, but had no concept of money.?" +
                    "946: It is against the law to use \"The Star Spangled Banner\" as dance music in Massachusetts.?" +
                    "947: Queen Cleopatra of Egypt was not actually Egyptian.?" +
                    "948: Early football fields were painted with both horizontal and vertical lines, creating a pattern that resembled a gridiron.?" +
                    "949: Two national capitals are named after U.S. presidents:  Washington, D.C., and Monrovia, the capital of Liberia.?" +
                    "950: The first spam message was transmitted over telegraph wires in 1864.?" +
                    "951: A pearl can be dissolved by vinegar.?" +
                    "952: Queen Isabella I of Spain, who funded Columbus' voyage across the ocean, claimed to have only bathed twice in her life.?" +
                    "953: The longest attack of hiccups ever lasted 68 years.?" +
                    "955: A bolt of lightning can reach temperatures hotter than 50,000 degrees Fahrenheit - five times hotter than the sun.?" +
                    "956: At the deepest point in the ocean, the water pressure is equivalent to having about 50 jumbo jets piled on top of you.?" +
                    "957: In only 7.6 billion years, the sun will reach its maximum size and will shine 3,000 times brighter.?" +
                    "958: The state of Alabama once financed the construction of a bridge by holding a rooster auction.?" +
                    "959: Federal law once allowed the government to quarantine people who came in contact with aliens.?" +
                    "960: There are 21 \"secret\" highways that are part of the Interstate Highway System. They are not identified as such by road signs.?" +
                    "961: The aphid insect is born pregnant.?" +
                    "962: John Wilkes Booth's brother saved the life of Abraham Lincoln's son.?" +
                    "964: It is illegal in the United Kingdom to handle salmon in suspicious circumstances.?" +
                    "965: It is illegal to play annoying games in the street in the United Kingdom.?" +
                    "966: Tennis was originally played with bare hands.?" +
                    "968: -40 degrees Fahrenheit is the same temperatures as -40 degrees Celsius.?" +
                    "969: U.S. President John Tyler had 15 children, the last of which was born when he was 70 years old.?" +
                    "970: Dolphins are unable to smell.?" +
                    "971: Charlie Chaplin failed to make the finals of a Charlie Chaplin look-alike contest.?" +
                    "972: The name of the city of Portland, Oregon was decided by a coin toss. The name that lost was Boston.?" +
                    "975: The letter J is the only letter in the alphabet that does not appear anywhere on the periodic table of the elements.?" +
                    "976: 'K' was chosen to stand for a strikeout in baseball because 'S' was being used to denote a sacrifice.?" +
                    "979: A dimpled golf ball produces less drag and flies farther than a smooth golf ball.?" +
                    "980: When grazing or resting, cows tend to align their bodies with the magnetic north and south poles.?" +
                    "981: President Chester A. Arthur owned 80 pairs of pants, which he changed several times per day.?" +
                    "982: Cows do not have upper front teeth.?" +
                    "983: Between 1979 and 1999, the planet Neptune was farther from the Sun than Pluto. This won't happen again until 2227.?" +
                    "984: When creating a mummy, Ancient Egyptians removed the brain by inserting a hook through the nostrils.?" +
                    "985: All of the major candidates in the 1992, 1996, and 2008 U.S. presidential elections were left-handed.?" +
                    "986: In Switzerland, it is illegal to own only one guinea pig because they are prone to loneliness.?" +
                    "987: The first American gold rush happened in North Carolina, not California.?" +
                    "989: To make one pound of honey, a honeybee must tap about two million flowers.?" +
                    "990: Chicago is named after smelly garlic that once grew in the area.?" +
                    "991: The Chicago river flows backwards; the flow reversal project was completed in 1900.?" +
                    "992: The patent for the fire hydrant was destroyed in a fire.?" +
                    "993: Powerful earthquakes can make the Earth spin faster.?" +
                    "994: Baby bunnies are called kittens.?" +
                    "995: A group of flamingos is called a flamboyance.?" +
                    "996: Sea otters hold each other’s paws while sleeping so they don’t drift apart.?" +
                    "997: Gentoo penguins propose to their life mates with a pebble.?" +
                    "998: Male pups will intentionally let female pups \"win\" when they play-fight so they can get to know them better.?" +
                    "999: A cat’s nose is ridged with a unique pattern, just like a human fingerprint.?" +
                    "1000: A group of porcupines is called a prickle.?" +
                    "1001: 99% of our solar system's mass is the sun.?" +
                    "1002: More energy from the sun hits Earth every hour than the planet uses in a year.?" +
                    "1003: If two pieces of the same type of metal touch in outer space, they will bond together permanently.?" +
                    "1004: Just a sugar cube of neutron star matter would weigh about one hundred million tons on Earth.?" +
                    "1005: A soup can full of neutron star material would have more mass than the Moon.?" +
                    "1006: Ancient Chinese warriors would show off to their enemies before battle, by juggling.?" +
                    "1007: OMG was added to dictionaries in 2011, but its first known use was in 1917.?" +
                    "1008: In the state of Arizona, it is illegal for donkeys to sleep in bathtubs.?" +
                    "1009: The glue on Israeli postage stamps is certified kosher.?" +
                    "1010: Rats and mice are ticklish, and even laugh when tickled.?" +
                    "1011: Norway once knighted a penguin.?" +
                    "1012: The King of Hearts is the only king without a mustache.?" +
                    "1013: It is illegal to sing off-key in North Carolina.?" +
                    "1014: Forty is the only number whose letters are in alphabetical order.?" +
                    "1015: One is the only number with letters in reverse alphabetical order.?" +
                    "1016: Strawberries are grown in every state in the U.S. and every province in Canada.?" +
                    "1017: The phrase, \"You’re a real peach\" originated from the tradition of giving peaches to loved ones.?" +
                    "1018: At latitude 60° south, it is possible to sail clear around the world without touching land.?" +
                    "1019: Interstate 90 is the longest U.S. Interstate Highway with over 3,000 miles from Seattle, WA to Boston, MA.?" +
                    "1020: DFW Airport in Texas is larger than the island of Manhattan.?" +
                    "1021: Benjamin Franklin invented flippers.?" +
                    "1022: Miami installed the first ATM for inline skaters.?" +
                    "1023: Indonesia is made up of more than 17,000 islands.?" +
                    "1024: Giraffes have the same number of vertebrae as humans:  7.?" +
                    "1025: The official taxonomic classification for llamas is Llama glama.?" +
                    "1026: Remove all the space between its atoms and Earth would be the size of a baseball.?" +
                    "1027: The soil on Mars is rust color because it's full of rust.?" +
                    "1028: Sound travels up to 15 times faster through steel than air, at speeds up to 19,000 feet per second.?" +
                    "1029: Humans share 50% of their DNA with bananas.?" +
                    "1030: Maine is the closest U.S. state to Africa.?" +
                    "1031: An octopus has three hearts.?" +
                    "1133: Only 12 U.S. presidents have been elected to office for two terms and served those two terms.?" +
                    "1134: Franklin D. Roosevelt was elected to office for four terms prior to the 22nd Amendment.?" +
                    "1135: John F. Kennedy, at 43, was the youngest elected president, and Ronald Reagan, at 73, the oldest.?" +
                    "1136: James Buchanan is the only bachelor to be elected president.?" +
                    "1137: Eight presidents have died while in office.?" +
                    "1138: Bill Clinton was born William Jefferson Blythe III, but took his stepfather’s last name when his mother remarried.?" +
                    "1139: Prior to the 12th Amendment in 1804, the presidential candidate who received the second highest number of electoral votes was vice president.?" +
                    "1140: George Washington was a successful liquor distributor, making rye whiskey, apple brandy, and peach brandy in his Mount Vernon distillery.?" +
                    "1141: Thomas Jefferson and John Adams chipped off a piece of Shakespeare's chair as a souvenir when they visited his home in 1786.?" +
                    "1142: George Washington started losing his permanent teeth in his 20s and had only one natural tooth by the time he was president.?" +
                    "1143: George Washington had false teeth made from many different materials, including an elephant tusk and hippopotamus ivory.?" +
                    "1144: George Washington protected his beloved horses from losing their teeth by making sure they were brushed regularly.?" +
                    "1145: John Quincy Adams regularly skinny-dipped in the Potomac River.?" +
                    "1146: Calvin Coolidge was so shy, he was nicknamed \"Silent Cal.\"?" +
                    "1147: Calvin Coolidge loved to wear a cowboy hat and ride his mechanical horse.?" +
                    "1148: President Herbert Hoover invented \"Hooverball\" (a cross between volleyball and tennis using a medicine ball), which he played with his cabinet members.?" +
                    "1149: Andrew Jackson was involved in as many as 100 duels, many of which were fought to defend the honor of his wife, Rachel.?" +
                    "1150: Martin Van Buren's nickname was \"Old Kinderhook\" because he was raised in Kinderhook, N.Y.?" +
                    "1151: James Buchanan bought slaves in Washington, D.C., and quietly freed them in Pennsylvania.?" +
                    "1152: Abraham Lincoln was only defeated once in about 300 wrestling matches, making it to the Wrestling Hall of Fame with honors as \"Outstanding American.\"?" +
                    "1153: In his youth, President Andrew Johnson apprenticed as a tailor.?" +
                    "1154: Ulysses S. Grant smoked at least 20 cigars a day; citizens sent him at least 10,000 boxes in gratitude after winning the Battle of Shiloh.?" +
                    "1155: Not only was James Garfield ambidextrous, he could write Latin with one hand and Greek with the other at the same time.?" +
                    "1156: Benjamin Harrison was the first president to have electricity in the White House; however, he was so scared of getting electrocuted, he’d never touch the light switches himself.?" +
                    "1157: William McKinley almost always wore a red carnation on his lapel as a good-luck charm.?" +
                    "1158: Herbert Hoover's son had two pet alligators that were occasionally permitted to run loose throughout the White House.?" +
                    "1159: Jimmy Carter filed a report for a UFO sighting in 1973, calling it \"the darndest thing I’ve ever seen.\"?" +
                    "1161: Bill Clinton's face is so symmetrical that he ranked in facial symmetry alongside male models.?" +
                    "1162: In 1916, Jeannette Rankin of Montana became the first woman elected to Congress.?" +
                    "1163: Gerald Ford was the only president and vice president never to be elected to either office.?" +
                    "1164: Victoria Woodhull, in 1872, was the first woman to run for the U.S. presidency.?" +
                    "1165: James Monroe received every electoral vote but one in the 1820 election.?" +
                    "1166: There are only three requirements to become U.S. president:  must be 35, a natural-born U.S. citizen, and have resided in the U.S. for at least 14 years.?" +
                    "1167: To cut groundskeeping costs during World War I, President Woodrow Wilson brought a flock of sheep to trim the White House grounds.?" +
                    "1168: Rutherford B. Hayes was the first president to use a phone, and his phone number was extremely easy to remember – simply \"1.\"?" +
                    "1169: Martin Van Buren was the first president born a U.S. citizen; all presidents before him were British.?" +
                    "1170: Andrew Jackson's pet parrot Poll was removed from his funeral for cursing.?" +
                    "1172: There has never been a U.S. president whose name started with the common letter S.?" +
                    "1173: Abraham Lincoln is the only U.S. president who was also a licensed bartender.?" +
                    "1174: Barack Obama is called the 44th president, but is actually the 43rd because Grover Cleveland is counted twice, as he was elected for two terms.?" +
                    "1175: Four times in U.S history has a presidential candidate won the popular vote but lost the election.?" +
                    "1176: President Herbert Hoover and his wife were fluent in Mandarin Chinese and would use it in the White House to speak privately to each other.?" +
                    "1177: November was chosen to be election month because it fell between harvest and brutal winter weather.?" +
                    "1178: Six of the last 12 U.S. presidents have been left-handed, far greater than the national average of lefties (10%).?" +
                    "1179: William Henry Harrison owned a pet goat while in office.?" +
                    "1180: John Adams had a horse named Cleopatra.?" +
                    "1181: James Madison had a pet parrot who outlived him and his wife.?" +
                    "1182: John Quincy Adams' wife raised silkworms.?" +
                    "1183: Martin Van Buren was given two tiger cubs while he was president.?" +
                    "1184: William Harrison had a billy goat at the White House.?" +
                    "1185: Franklin Pierce was gifted two small \"sleeve dogs\" – he kept one and gave the other to Jefferson Davis.?" +
                    "1186: Abraham Lincoln's son had a pet turkey, which he gave a pardon so it wasn't killed and eaten.?" +
                    "1187: James Garfield had a dog appropriately named Veto.?" +
                    "1188: William Taft liked milk so much that he had cows graze on the White House lawn, Pauline being the last in history to graze there.?" +
                    "1189: Calvin Coolidge had a bulldog named Boston Beans, a terrier named Peter Pan, and a pet raccoon.?" +
                    "1190: John Kennedy had a pony named Macaroni.?" +
                    "1191: Lyndon Johnson had two beagles, named Him and Her, for which he was criticized for picking up by their ears.?" +
                    "1192: Jimmy Carter had a dog named Grits, a gift given to his daughter Amy.?" +
                    "1193: Bill Clinton had a cat named Socks, which was the first presidential pet to have its own website.?" +
                    "1194: Woodrow Wilson passed the Georgia Bar Exam despite not finishing law school; he also has a PhD.?" +
                    "1195: President Zachary Taylor's nickname was \"Old Rough and Ready\" because of his famed war career.?" +
                    "1196: Andrew Jackson was once given a 1,400-pound cheese wheel as a gift, which he served at his outgoing President's Reception.?" +
                    "1197: Blueberry jelly beans were created for Ronald Reagan’s presidential inauguration in 1981.?" +
                    "1198: Dwight D. Eisenhower was the first Texas-born president.?" +
                    "1199: Lyndon Johnson's family all had the initials LBJ.?" +
                    "1200: Thomas Jefferson was convinced that if he soaked his feet in a bucket of cold water every day, he’d never get sick.?" +
                    "1201: Gerald Ford worked as a fashion model during college and actually appeared on the cover of Cosmopolitan.?" +
                    "1202: Dwight Eisenhower was the only president to serve in both World War I and World War II.?" +
                    "1203: Jimmy Carter was the first president to be born in a hospital.?" +
                    "1204: Calvin Coolidge liked to have his head rubbed with petroleum jelly while eating breakfast in bed, believing it was good for his health.?" +
                    "1205: A portion of Grover Cleveland's jaw was artificial, composed of vulcanized rubber.?" +
                    "1206: Russia and the United States are less than three miles apart.?" +
                    "1207: John Adams and Thomas Jefferson died within hours of each other on the Fourth of July in 1826.?" +
                    "1208: Abraham Lincoln's dog Fido was the first \"First Dog\" to be photographed.?" +
                    "1209: President Calvin Coolidge owned two lion cubs:  Tax Reduction and Budget Bureau.?" +
                    "1210: President Rutherford B. Hayes' cat Siam was the first Siamese cat in the U.S.?" +
                    "1211: President John Quincy Adams' pet alligator lived in a White House bathroom.?" +
                    "1212: First Lady Abigail Adams famously wrote, \"If you love me...you must love my dog.\"?" +
                    "1213: John Adams' pets Satan and Juno were the first dogs to live in the White House.?" +
                    "1214: Calvin Coolidge walked pet raccoon Rebecca on a leash around the White House.?" +
                    "1215: More presidents have had pet birds than cats.?" +
                    "1216: Thomas Jefferson's pet mockingbird was trained to eat out of his mouth.?" +
                    "1217: Spotty Bush, an English Springer Spaniel, has been the only presidential pet to live at the White House during two different administrations.?" +
                    "1218: Andrew Jackson was the first president to ride on a railroad train.?" +
                    "1219: Pat Nixon was the first First Lady to wear pants in public.?" +
                    "1220: First Lady Martha Washington was the first American woman to be honored on a U.S. postage stamp.?" +
                    "1221: When snakes are born with two heads, they fight each other for food.?" +
                    "1222: Venus is the only planet to rotate clockwise.?" +
                    "1223: Tennessee ties with Missouri as the most neighborly state, bordered by 8 states.?" +
                    "1224: The cotton candy machine was invented in 1897, by a dentist.?" +
                    "1225: You can’t hum while plugging your nose.?" +
                    "1226: Elephants are afraid of bees.?" +
                    "1227: They used to offer goat carriage rides in Central Park.?" +
                    "1228: Chimps can develop their own fashion trends.?" +
                    "1229: Monday is the only day of the week with an anagram:  dynamo.?" +
                    "1230: The only Michelangelo painting in the Western Hemisphere is on display in Fort Worth, TX.?" +
                    "1231: Humans are 1-2 centimeters taller in the morning than at night.?" +
                    "1232: Baby giraffes fall up to 6 feet to the ground when they are born.?" +
                    "1233: It takes around 200 muscles to take a step.?" +
                    "1234: The flamingo can only eat when its head is upside down.?" +
                    "1235: A bald eagle nest can weigh up to two tons.?" +
                    "1236: Worrying squirrels is not tolerated in Missouri.?" +
                    "1237: Wombat droppings are cube-shaped.?" +
                    "1238: Adult humans are the only mammal that can't breathe and swallow at the same time.?" +
                    "1239: Hens do not need a rooster to lay an egg.?" +
                    "1240: There are more nerve connections or \"synapses\" in your brain than there are stars in our galaxy.?" +
                    "1241: There are more English words beginning with the letter \"S\" than any other letter.?" +
                    "1242: There are more fake than real flamingos.?" +
                    "1243: The word \"bride\" comes from an old Proto-Germanic word meaning \"to cook.\"?" +
                    "1244: The word utopia – an ideal place – ironically comes from a Greek word meaning \"no place.\"?" +
                    "1245: Los Angeles was originally founded as El Pueblo de la Reina de Los Angeles.?" +
                    "1246: The woolly mammoth still roamed the earth while the pyramids were being built.?" +
                    "1247: Nine-banded armadillos almost always give birth to four identical quadruplets.?" +
                    "1248: Jellyfish don’t have brains.?" +
                    "1249: Jellyfish can clone themselves.?" +
                    "1250: The koala is the longest-sleeping animal, sleeping an average of 22 hours per day.?" +
                    "1251: Walruses are true party animals; they can go without sleep for 84 hours.?" +
                    "1252: The city of Chicago was raised by over a foot during the 1850s and ’60s without disrupting daily life.?" +
                    "1253: Red kangaroos can hop up to 44 mph.?" +
                    "1254: Arkansas has the only active diamond mine in the United States.?" +
                    "1255: Robert Heft, who designed the current U.S. flag in a high school project, received a B- because it \"lacked originality.\"?" +
                    "1256: The first 18-hole golf course in America was built on a sheep farm in 1892.?" +
                    "1257: Most newborns will lose all the hair they are born with in the first six months of life.?" +
                    "1258: Ripening bananas glow an intense blue under black light.?" +
                    "1259: Coconut water was used as an IV drip in WWII when saline solution was in short supply.?" +
                    "1260: Mercury and Venus are the only planets in our solar system with no moon.?" +
                    "1261: Peanuts are not actually nuts but legumes.?" +
                    "1262: The Oscar statuette is brittanium plated with 24K gold.?" +
                    "1263: The only thing that can scratch a diamond is a diamond.?" +
                    "1264: There is a star that is a diamond of ten billion trillion trillion carats.?" +
                    "1265: One ounce of gold can be stretched into a thin wire measuring 50 miles.?" +
                    "1266: A $100,000 bill exists, but was only used by Federal Reserve Banks.?" +
                    "1267: 10 million bricks were used to build the Empire State Building.?" +
                    "1268: One quarter of all the body’s bones are in the feet.?" +
                    "1269: Lake Havasu City, AZ, has been recorded as the hottest city in the U.S. with average summer temperatures of 94.6.?" +
                    "1270: Early sunscreens included ingredients like rice bran oil, iron, clay, and tar.?" +
                    "1271: One of the first sunscreens was sold in the 1910s under the name Zeozon.?" +
                    "1272: In the U.S., there is an official rock, paper, scissors league.?" +
                    "1273: The largest bill ever issued by the U.S. was a $100,000 bill in 1934.?" +
                    "1274: Kickball is referred to as \"soccer-baseball\" in some parts of Canada.?" +
                    "1275: Less than 1% of Sweden’s household waste ends up in a dump.?" +
                    "1276: Duck Duck Goose is called Duck Duck Grey Duck in Minnesota.?" +
                    "1277: There are more tigers owned by Americans than in the wild worldwide.?" +
                    "1278: Hawaiian pizza was actually created in Canada.?" +
                    "1279: A city in Greece struggles to build subway systems because they keep digging up ancient ruins.?" +
                    "1280: Elvis was a natural blonde.?" +
                    "1281: On Venus, it snows metal.?" +
                    "1282: Eating 600 bananas is the equivalent of one chest X-ray in terms of radiation.?" +
                    "1283: The potato became the first vegetable to be grown in space.?" +
                    "1284: The average dog can understand over 150 words.?" +
                    "1285: At one time, serving ice cream on cherry pie in Kansas was prohibited.?" +
                    "1286: Blueberries are one of the only natural foods that are truly blue in color.?" +
                    "1287: Blueberries are also called \"star berries.\"?" +
                    "1288: There are more varieties of blueberries than states in the U.S.?" +
                    "1289: Typically, blueberries become ripe after 2-5 weeks on a bush.?" +
                    "1290: Love blueberries. Celebrate them all year round, but especially in July, National Blueberry Month.?" +
                    "1291: While blueberries grow in clusters on their bush, the individual blueberries ripen at different times.?" +
                    "1292: The first commercial batch of blueberries came from Whitesbog, New Jersey, in 1916.?" +
                    "1293: The perfect blueberry should be \"dusty\" in color.?" +
                    "1294: Maine produces more wild blueberries than anywhere else in the world.?" +
                    "1295: 75% of the U.S.’s tart cherries come from Michigan.?" +
                    "1296: Traverse, MI, considers itself the Cherry Capital of the World.?" +
                    "1297: Once cherries have been picked, they don’t ripen.?" +
                    "1298: Make sure to eat a chocolate-covered cherry on January 3; it’s National Chocolate-Covered Cherry Day.?" +
                    "1299: On average, how many cherries are in a pound? 44.?" +
                    "1300: The word \"cherry\" comes from the Turkish town of Cerasus.?" +
                    "1301: A cherry pie is made of about 250 cherries.?" +
                    "1302: Eau Claire, Michigan, is known as \"The Cherry Pit Spitting Capital of the World.\"?" +
                    "1303: The National Anthem of Greece has 158 verses.?" +
                    "1304: North Korea and Finland are technically separated by only one country.?" +
                    "1305: Australia’s first police force was made up of the most well-behaved convicts.?" +
                    "1306: Emergency phone number in Europe is 112.?" +
                    "1307: Canada's postal code for Santa Claus at the North Pole is H0H 0H0.?" +
                    "1308: Russia has a larger surface area than Pluto.?" +
                    "1309: In New Zealand, it is illegal to name your twin babies \"Fish\" and \"Chips.\"?" +
                    "1310: Chocolate bars and blue denim both originated in Guatemala.?" +
                    "1311: In New Zealand, parents have to run baby names by the government for approval.?" +
                    "1312: When a child loses their tooth in Greece, they throw it on the roof as a good luck wish that their adult teeth will be strong.?" +
                    "1313: Australia is the only nation to govern an entire continent and its outlying islands.?" +
                    "1314: No one in Greece can choose not to vote; voting is required by law for every citizen who is 18 or older.?" +
                    "1315: Australia has 10,685 beaches; you could visit a new beach every day for more than 29 years.?" +
                    "1316: China is large enough to cover about five separate time zones, but only has one national time zone since the Chinese Civil War in 1949.?" +
                    "1317: There is a language in Botswana that consists of mainly five types of clicks.?" +
                    "1318: An African elephant can turn the pages of a book with its trunk.?" +
                    "1319: Ancient Egyptians slept on head rests made of wood, ivory, or stone.?" +
                    "1320: A traffic jam once lasted for 11 days in Beijing, China.?" +
                    "1321: Alaska is the only state that can be typed on one row of keys.?" +
                    "1322: The blue in the Sistine Chapel is made of ground lapis lazuli gems and oils.?" +
                    "1323: \"The Bridge of Eggs\" built in Lima, Peru, was made of mortar that was mixed with egg whites.?" +
                    "1324: In South Korea, you are one year old at birth.?" +
                    "1325: The Great Wall of China is 13,170.7 miles long, over five times the distance from LA to NYC.?" +
                    "1326: The horizontal line between two numbers in a fraction is called a vinculum.?" +
                    "1327: The metal ring on the end of a pencil is called a ferrule.?" +
                    "1328: You cannot taste food until mixed with saliva.?" +
                    "1329: There is an uninhabited island in the Bahamas known as Pig Beach, which is populated entirely by swimming pigs.?" +
                    "1330: Lake Hillier, in Western Australia, is colored a bright pink.?" +
                    "1331: Spiked dog collars were invented by the Ancient Greeks, who used them on their sheepdogs to protect their necks from wolves.?" +
                    "1332: \"Buffalo buffalo Buffalo buffalo buffalo buffalo Buffalo buffalo.\" is a grammatically correct sentence.?" +
                    "1333: On Jupiter and Saturn, it rains diamonds.?" +
                    "1334: Nowhere in the Humpty Dumpty nursery rhyme does it say that Humpty Dumpty is an egg.?" +
                    "1336: Located on the Detroit River, the J.W. Wescott II is the only floating post office in the U.S. and has its own ZIP Code:  48222.?" +
                    "1337: Antarctica is the largest desert in the world.?" +
                    "1338: Tomatoes have more genes than humans.?" +
                    "1339: In Texas, it is legal to kill Bigfoot if you ever find it.?" +
                    "1340: Elephants can smell water up to 3 miles away.?" +
                    "1341: A snail can grow back a new eye if it loses one.?" +
                    "1342: You can tell a turtle’s gender by the noise it makes. Males grunt and females hiss.?" +
                    "1343: French poodles actually originated in Germany.?" +
                    "1344: Marine mammals swim by moving their tails up and down, while fish swim by moving their tails left and right.?" +
                    "1345: \"Knocker uppers\" were professionals paid to shoot peas at windows. They were replaced by alarm clocks.?" +
                    "1346: An average cumulus cloud weighs more than 70 adult T. rexes.?" +
                    "1347: Clicking your computer mouse 1,400 times burns one calorie.?" +
                    "1348: \"Guy\" was once an insult for anyone dressed in poor clothes, originating from the burning of effigies of the infamous British rebel, Guy Fawkes.?" +
                    "1349: The national animal of Scotland is the unicorn.?" +
                    "1350: The tea bag was created by accident in 1908 by Thomas Sullivan of New York.?" +
                    "1351: The male ostrich can roar just like a lion.?" +
                    "1352: A group of frogs is called an army.?" +
                    "1353: Corn always has an even number of rows on each ear.?" +
                    "1354: You are always looking at your nose; your brain just chooses to ignore it.?" +
                    "1355: There is a single mega-colony of ants that spans three continents, covering much of Europe, the west coast of the U.S., and the west coast of Japan.?" +
                    "1356: The world's largest mountain range is under the sea.?" +
                    "1357: The Anglo-Zanzibar war of 1896 is the shortest war on record, lasting an exhausting 38 minutes.?" +
                    "1358: Below the Kalahari Desert lies the world's largest underground lake.?" +
                    "1359: Oregon and Mexico once shared a border.?" +
                    "1360: Bluetooth technology was named after a 10th century Scandinavian king.?" +
                    "1361: A nun held one of the first PhDs in computer science.?" +
                    "1362: For 67 years, Nintendo only produced playing cards.?" +
                    "1363: The ancient Chinese carried Pekingese puppies in the sleeves of their robes.?" +
                    "1364: A tarantula can survive for more than two years without food.?" +
                    "1365: Ethiopia follows a calendar that is seven years behind the rest of the world.?" +
                    "1366: In Denmark, citizens have to select baby names from a list of 7,000 government-approved names.?" +
                    "1367: Every tweet Americans send is archived by the Library of Congress.?" +
                    "1368: A neuron star is as dense as stuffing 50 million elephants into a thimble.?" +
                    "1369: More energy from the sun hits Earth every hour than the planet uses in a year.?" +
                    "1370: An earthquake in 1812 caused the Mississippi River to flow backward.?" +
                    "1371: In 2014, the Department of Veterans Affairs was still paying a Civil War pension.?" +
                    "1372: In Webster's Dictionary, the longest words without repeating letters are \"uncopyrightable\" and \"dermatoglyphics.\"?" +
                    "1373: \"Unprosperousness\" is the longest word in which no letter occurs only once.?" +
                    "1374: \"Typewriter\" and \"perpetuity\" are the longest words that can be typed on a single line of a QWERTY keyboard.?" +
                    "1375: There have been three Olympic games held in countries that no longer exist.?" +
                    "1376: Golf is the only sport to be played on the moon.?" +
                    "1377: The word \"checkmate\" comes from the Persian phrase meaning \"the king is dead.\"?" +
                    "1378: The brain is the only organ in the human body without pain receptors.?" +
                    "1379: There is a volcano on Mars the size of Arizona.?" +
                    "1380: The blue whale can produce the loudest sound of any animal. At 188 decibels, the noise can be detected over 800 kilometers away.?" +
                    "1381: Dogs’ sense of hearing is more than ten times more acute than a human’s.?" +
                    "1382: A housefly hums in the key of F.?" +
                    "1383: Venus is the only planet in the solar system where the sun rises in the west.?" +
                    "1384: The state animal of Tennessee is a raccoon.?" +
                    "1385: If you were to stretch out a Slinky until it’s flat, it would measure 87 feet long.?" +
                    "1386: It's illegal in many countries to perform surgery on an octopus without anesthesia because of its intelligence.?" +
                    "1387: There are more trees on Earth than stars in the galaxy.?" +
                    "1388: Human thigh bones are stronger than concrete.?" +
                    "1389: Fires spread faster uphill than downhill.?" +
                    "1390: The Florida Everglades is the only place in the world where both alligators and crocodiles live together.?" +
                    "1391: Newborns can't cry actual tears. This normally occurs between 3 weeks and 3 months of life.?" +
                    "1392: If you could drive your car upward, you would be in space in less than an hour.?" +
                    "1393: The sun is actually white, but the Earth’s atmosphere makes it appear yellow.?" +
                    "1394: The Earth rotates at a speed of 1,040 MPH.?" +
                    "1395: Even when a snake has its eyes closed, it can still see through its eyelids.?" +
                    "1396: The word \"aegilops\" is the longest word in the English language to have all of its letters in alphabetical order.?" +
                    "1397: Gorillas burp when they are happy.?" +
                    "1398: Because of metal prices, since 2006 the U.S. Mint has had to spend more to make a penny than they are worth.?" +
                    "1399: \"Never odd or even\" spelled backward is still \"Never odd or even.\"?" +
                    "1400: In Alabama, it's illegal to carry an ice cream cone in your back pocket at any time.?" +
                    "1401: Alaska is the most northern, western, and eastern U.S. state.?" +
                    "1402: In France, it's illegal for employers to send emails after work hours.?" +
                    "1403: A group of raccoons is called a gaze.?" +
                    "1404: Pteronophobia is the fear of being tickled by feathers.?" +
                    "1405: Cherophobia is the fear of happiness.?" +
                    "1406: The vertical distance between the Earth's highest and lowest points is about 12 miles.?" +
                    "1407: A flock of crows is known as a murder.?" +
                    "1408: Dr. Seuss wrote \"Green Eggs and Ham\" to win a bet with his publisher who thought he could not complete a book with only 50 words.?" +
                    "1409: Over 80% of the land in Nevada is owned by the U.S. government.?" +
                    "1410: There are more people on Facebook today than there were on the Earth 200 years ago.?" +
                    "1415: Mangoes have noses.?" +
                    "1416: Mangoes can get sunburned.?" +
                    "1417: Before 1859, baseball umpires sat behind home plate in rocking chairs.?" +
                    "1418: The shortest professional baseball player was 3 feet, 7 inches tall.?" +
                    "1419: The average life span of an MLB baseball is five to seven pitches.?" +
                    "1420: The most valuable baseball card ever is worth about $2.8 million.?" +
                    "1421: The paisley pattern is based on the mango.?" +
                    "1422: In India, mango leaves are used to celebrate the birth of a boy.?" +
                    "1423: A flipped coin is more likely to land on the side it started on.?" +
                    "1424: When sprinting, professional cyclists produce enough power to power a home.?" +
                    "1425: Mosquitoes prefer to bite people with Type O blood.?" +
                    "1426: During a typical MLB season, approximately 160,000 baseballs are used.?" +
                    "1427: The Bible is the world's most shoplifted book.?" +
                    "1428: The British pound is the world's oldest currency still in use.?" +
                    "1429: The Great Lakes have more than 30,000 islands.?" +
                    "1430: Mountain lions can whistle.?" +
                    "1431: While rabbits have near-perfect 360-degree panoramic vision, their most critical blind spot is directly in front of their nose.?" +
                    "1432: When a koala is born, it is about the size of a jelly bean.?" +
                    "1433: Toe wrestling is a competitive sport.?" +
                    "1434: There have been 85 recorded instances of a pitcher striking out four batters in one inning.?" +
                    "1435: 3.7 million bags of ballpark peanuts are eaten every year at ballparks.?" +
                    "1436: Shakespeare created the name Jessica for his play \"The Merchant of Venice.\"?" +
                    "1437: Tooth enamel is the hardest substance in the human body.?" +
                    "1438: The mummy of Pharaoh Ramesses II has a passport.?" +
                    "1439: It is physically impossible for a pig to look at the sky.?" +
                    "1440: There are more stars in the universe than grains of sand on earth.?" +
                    "1441: A caterpillar has more muscles than a human.?" +
                    "1442: A shrimp's heart is in its head.?" +
                    "1443: A human being could swim through the blood vessels of a blue whale.?" +
                    "1444: Light could travel around the earth nearly 7.5 times in one second.?" +
                    "1445: A single lightning bolt contains enough energy to cook 100,000 pieces of toast.?" +
                    "1446: About one in every 2,000 babies is born with teeth.?" +
                    "1447: Water can boil and freeze at the same time.?" +
                    "1448: Less than 5% of the population needs just 4-5 hours of sleep.?" +
                    "1449: Peanut butter can be converted into diamonds.?" +
                    "1450: Astronauts can't burp in space.?" +
                    "1451: An Immaculate Inning is when a pitcher strikes out three batters with only nine pitches.?" +
                    "1452: Earth is the only planet not named after a Greek or Roman god.?" +
                    "1453: Yawns are contagious to dogs as well as humans.?" +
                    "1454: In the 1960s, the U.S. government tried to turn a cat into a spy.?" +
                    "1455: Movie trailers used to come on at the end of movies, but no one stuck around to watch them.?" +
                    "1456: MLB umpires often wear black underwear, in case they split their pants.?" +
                    "1457: It is possible to record four outs in one-half inning of baseball.?" +
                    "1458: There are nine different ways to reach first base.?" +
                    "1459: During World War II, the U.S. military designed a grenade to be the size and weight of a baseball, since \"any young American man should be able to properly throw it.\"?" +
                    "1460: Philadelphia zookeeper Jim Murray sent baseball scores to telegraph offices by carrier pigeon every half inning in 1883.?" +
                    "1461: From 1845 through 1867, home base was circular, made of iron, painted or enameled white, and 12 inches in diameter.?" +
                    "1462: President Bill Clinton's first presidential pitch (on April 4, 1993) was the first ever from the pitcher's mound to the catcher's mitt.?" +
                    "1463: Thunder is actually the sound caused by lightning.?" +
                    "1464: Australia is wider than the moon.?" +
                    "1465: 85% of people only breathe out of one nostril at a time.?" +
                    "1466: An albatross can sleep while it flies.?" +
                    "1467: In a room of 23 people, there is a 50% chance that two people have the same birthday.?" +
                    "1468: Bubble wrap was originally invented as a wallpaper in 1957.?" +
                    "1469: There is a species of jellyfish that is immortal.?" +
                    "1470: Of the 193 members of the United Nations, Britain has invaded 171 of them.?" +
                    "1471: The Apollo 11 guidance computer was no more powerful than today's pocket calculator.?" +
                    "1472: \"Sphenopalatine ganglioneuralgia\" is the technical name for brain freeze.?" +
                    "1473: Earth is actually located inside the sun's atmosphere.?" +
                    "1474: The spiral shapes of sunflowers follow the Fibonacci sequence.?" +
                    "1475: If you drilled a hole through the earth, it would take 42 minutes to fall through it.?" +
                    "1476: The planet 55 Cancri e is made of diamonds and would be worth $26.9 nonillion.?" +
                    "1477: France used the guillotine as recently as 1977.?" +
                    "1478: Sloths move so slow that algae can grow on them.?" +
                    "1479: Zero is the only number that cannot be represented by Roman numerals.?" +
                    "1480: Michelangelo hated painting and wrote a poem about it.?" +
                    "1481: The dwarf lantern shark grows to be no bigger than a human hand.?" +
                    "1482: \"Tools of ignorance\" is a nickname for the equipment worn by catchers.?" +
                    "1483: More than 100 baseballs are used during a typical MLB game.?" +
                    "1484: Pitchers were prohibited from delivering the ball overhand for much of the 19th century.?" +
                    "1485: Walks were scored as hits during the 1887 season.?" +
                    "1486: A regulation baseball has 108 stitches.?" +
                    "1487: A \"can of corn\" is a routine fly ball hit to an outfielder.?" +
                    "1488: Baseball is played in more than 100 countries.?" +
                    "1489: \"Take Me Out to the Ballgame\" was written in 1908 by Jack Norworth and Albert Von Tilzer, both of whom had never been to a baseball game.?" +
                    "1490: A baseball pitcher’s curveball can break up to 17 inches.?" +
                    "1491: MLB baseballs are rubbed in Lena Blackburne Baseball Rubbing Mud, a unique mud found only near Palmyra, New Jersey.?" +
                    "1492: The Metropolitan Museum of Art has over 30,000 baseball cards as part of the Jefferson R. Burdick collection.?" +
                    "1493: William Howard Taft, the 27th president of the U.S., began the tradition of throwing out the ceremonial first pitch in 1910.?" +
                    "1494: MLB National League (1876) is the oldest professional sports league that is still in existence.?" +
                    "1495: The first modern-day World Series game was played in 1903.?" +
                    "1496: The Mendoza Line is a .200 batting average.?" +
                    "1497: There are 13 different pitches a pitcher can throw in baseball.?" +
                    "1498: The first MLB All-Star Game was played in 1933.?" +
                    "1499: A player was once ejected from an MLB game for sleeping during the game.?" +
                    "1500: Baseball hits that bounced over the fence were considered home runs until the 1930s.?" +
                    "1501: The most home runs ever recorded in an MLB season is 73.?" +
                    "1502: The highest batting average ever recorded in an MLB season is .440.?" +
                    "1503: MLB has not had a lefty play catcher since 1989.?" +
                    "1504: The longest MLB game went 26 innings.?";

    public static String randomfact()
    {
        String randofact = "";
        String[] factlist = facts.split("\\?");
        String[] factlist2 = facts2.split("\\?");
        Random rand = new Random();
        Random r = new Random();
        int n = rand.nextInt(1);
        if (n == 1)
        {
            randofact = factlist[r.nextInt(factlist.length)];
        }
        if (n == 0)
        {
            randofact = factlist2[rand.nextInt(factlist2.length)];
        }
        randofact.replaceAll("\\?", "");
        randofact = "Fact #" + randofact;
        return randofact;


    }

    @Override
    public String getDescription()
    {
        return "Spammer: Share awesome snapple facts with the rest of the world!";
    }

}
