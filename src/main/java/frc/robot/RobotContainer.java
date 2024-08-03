// Copyright 2021-2024 FRC 6328
// http://github.com/Mechanical-Advantage
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// version 3 as published by the Free Software Foundation or
// available in the root directory of this project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.

package frc.robot;

import java.util.List;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.commands.PathPlannerAuto;
import com.pathplanner.lib.path.GoalEndState;
import com.pathplanner.lib.path.PathConstraints;
import com.pathplanner.lib.path.PathPlannerPath;

import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.commands.PathPlannerAuto;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.Constants.DriveConstants;
import frc.robot.Constants.RobotMap;
import frc.robot.subsystems.climber.Climb;
import frc.robot.subsystems.climber.ClimbIOReplay;
import frc.robot.subsystems.climber.ClimbIOSim;
import frc.robot.subsystems.climber.ClimbIOSparkMax;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.GyroIOPigeon2Phoenix6;
import frc.robot.subsystems.drive.GyroIOReplay;
import frc.robot.subsystems.drive.ModuleIOReal;
import frc.robot.subsystems.drive.ModuleIOReplay;
import frc.robot.subsystems.drive.ModuleIOSim;
import frc.robot.subsystems.feeder.BeambreakIOReal;
import frc.robot.subsystems.feeder.BeambreakIOReplay;
import frc.robot.subsystems.feeder.BeambreakIOSim;
import frc.robot.subsystems.feeder.Feeder;
import frc.robot.subsystems.feeder.FeederIOReplay;
import frc.robot.subsystems.feeder.FeederIOSim;
import frc.robot.subsystems.feeder.FeederIOSparkMax;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.intake.IntakeIOReplay;
import frc.robot.subsystems.intake.IntakeIOSim;
import frc.robot.subsystems.intake.IntakeIOSparkMax;
import frc.robot.subsystems.pivot.Pivot;
import frc.robot.subsystems.pivot.PivotIOReplay;
import frc.robot.subsystems.pivot.PivotIOSim;
import frc.robot.subsystems.pivot.PivotIOSparkMax;
import frc.robot.subsystems.shooter.Shooter;
import frc.robot.subsystems.shooter.ShooterIOReplay;
import frc.robot.subsystems.shooter.ShooterIOSim;
import frc.robot.subsystems.shooter.ShooterIOSparkMax;

/**
 * This class is where the bulk of the robot should be declared. Since
 * Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in
 * the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of
 * the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  private Driver driver   = Driver.ARNAV_DRIVE;
  private Driver operator = Driver.ZACH_OPERATOR;

  // Subsystems
  // private final SwerveSubsystem m_drive = new SwerveSubsystem(new
  // File(Filesystem.getDeployDirectory(),
  // "swerve/swerve"));;
  private final Drive m_drive;
  private final Climb m_climber;
  private final Intake m_intake;
  private final Feeder m_feeder;
  private final Pivot m_pivot;
  private final Shooter m_shooter;
  private final Visualizer m_visualizer;

  // Controller
  private final CommandXboxController m_driver = new CommandXboxController(0);
  private final CommandXboxController m_operator = new CommandXboxController(1);

  // final CommandXboxController driverXbox = new CommandXboxController(0);
  // private final SwerveSubsystem drivebase

  /**
   * The container for the robot. Contains subsystems, OI devices, and commands.
   */
  public RobotContainer() {
    switch (Constants.currentMode) {
      case REAL:
        // Real robot, instantiate hardware IO implementations
        m_drive = new Drive(
            new GyroIOPigeon2Phoenix6(),
            new ModuleIOReal(0),
            new ModuleIOReal(1),
            new ModuleIOReal(2),
            new ModuleIOReal(3));
        m_climber = new Climb(new ClimbIOSparkMax());
        m_intake = new Intake(new IntakeIOSparkMax());
        m_feeder = new Feeder(new FeederIOSparkMax(), new BeambreakIOReal(RobotMap.Shooter.feederBeambreak),
            new BeambreakIOReal(RobotMap.Shooter.shooterBeambreak));
        m_pivot = new Pivot(new PivotIOReplay());
        m_shooter = new Shooter(new ShooterIOSparkMax());
        break;

      case SIM:
        // Sim robot, instantiate physics sim IO implementations
        m_drive = new Drive(
            new GyroIOReplay() {},
            new ModuleIOSim("FrontLeft"),
            new ModuleIOSim("FrontRight"),
            new ModuleIOSim("BackLeft"),
            new ModuleIOSim("BackRight"));
        m_climber = new Climb(new ClimbIOSim());
        m_intake = new Intake(new IntakeIOSim());
        m_feeder = new Feeder(new FeederIOSim(), new BeambreakIOSim(RobotMap.Shooter.feederBeambreak),
            new BeambreakIOSim(RobotMap.Shooter.shooterBeambreak));
        m_pivot = new Pivot(new PivotIOReplay());
        m_shooter = new Shooter(new ShooterIOSim());
        break;

      default:
        // Replayed robot, disable IO implementations
        m_drive = new Drive(
            new GyroIOReplay() {},
            new ModuleIOReplay() {},
            new ModuleIOReplay() {},
            new ModuleIOReplay() {},
            new ModuleIOReplay() {});
        m_climber = new Climb(new ClimbIOReplay());
        m_intake = new Intake(new IntakeIOReplay());
        m_feeder = new Feeder(new FeederIOReplay(), new BeambreakIOReplay(), new BeambreakIOReplay());
        m_pivot = new Pivot(new PivotIOReplay());
        m_shooter = new Shooter(new ShooterIOReplay());
        break;

    }
    m_visualizer = new Visualizer(m_climber, m_intake, m_pivot);

    // Registering named commands
    NamedCommands.registerCommand("ShootNote", Commands.sequence(
        Commands.deadline(
            new WaitCommand(2),
            m_shooter.setRPM(() -> 5000, 0.3),
            Commands.sequence(
                new WaitCommand(1),
                m_feeder.setRPM(() -> 3000)
            )
        ),
        Commands.deadline(
            new WaitCommand(1),
            m_shooter.setRPM(() -> 0, 0.3),
            m_feeder.setRPM(() -> 0)
        )
    ));

    // Configure the button bindings
    configureButtonBindings();

    // Configuring Pathplanner commands
    configurePathplanner();
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be
   * created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing
   * it to a {@link
   * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {

    // switch(driver) {
    //   case ARNAV_DRIVE:
    //     break;
    //   case CONNOR_DRIVE:
    //     break;
    //   case JAMES_DRIVE:
    //     break;
    //   case RAM_DRIVE:
    //     break;
    //   case ZACH_DRIVE:
    //     break;
    // }

    // switch (operator) {
    //   case ARNAV_OPERATOR:
    //     break;
    //   case CONNOR_OPERATOR:
    //     break;
    //   case JAMES_OPERATOR:+
    //     break;
    //   case RAM_OPERATOR:
    //     break;
    //   case ZACH_OPERATOR:
    //     break;
    // }

    // Driver Controller

    // joysticks for drive
    m_drive.setDefaultCommand(
        m_drive.runVoltageTeleopFieldRelative(
            () -> new ChassisSpeeds(
                -teleopAxisAdjustment(m_driver.getLeftY()) *
                    DriveConstants.maxLinearVelocity,
                -teleopAxisAdjustment(m_driver.getLeftX()) *
                    DriveConstants.maxLinearVelocity,
                -teleopAxisAdjustment(m_driver.getRightX())
                    * DriveConstants.maxLinearVelocity)));

    // left trigger -> climb up
    m_driver.leftTrigger(0.1).onTrue(
        m_climber.setDutyCycle(-1)
    ).onFalse(
        m_climber.setDutyCycle(0)
    );

    // left bumper -> climb down
    m_driver.leftBumper().onTrue(
        m_climber.setDutyCycle(-1)
    ).onFalse(
        m_climber.setDutyCycle(0)
    );

    // right trigger -> climb up
    m_driver.rightTrigger(0.1).onTrue(
        m_climber.setDutyCycle(1)
    ).onFalse(
        m_climber.setDutyCycle(0)
    );

    // left trigger -> climb up
    m_driver.rightBumper().onTrue(
        m_climber.setDutyCycle(1)
    ).onFalse(
        m_climber.setDutyCycle(0)
    );

    // Operator Controller

    // D-Pad Up for intake down, rollers forward, until note in feeder beambreak
    m_operator.povUp().onTrue(
        Commands.parallel(
            m_intake.setIntakeDown(false)))
            // m_feeder.setRPM(() -> 3000)).until(() -> m_feeder.feederBeambreakObstructed()))
      .onFalse(
        Commands.parallel(
            m_intake.setIntakeDown(false),
            m_feeder.setRPM(() -> 5000)).until(() -> m_feeder.feederBeambreakObstructed()))
        .onFalse(m_intake.setIntakeUp());

    // D-Pad Down for intake down, rollers backward
    m_operator.povDown().onTrue(
        Commands.parallel(
            m_intake.setIntakeDown(true),
            m_feeder.setRPM(() -> -3000)))
        .onFalse(m_intake.setIntakeUp());

    // Right trigger for run intake forward
    m_operator.rightTrigger(0.1).onTrue(
        Commands.parallel(
            m_intake.setRollerRPM(() -> 3000),
            m_feeder.setRPM(() -> 3000)).until(() -> m_feeder.feederBeambreakObstructed()))
        .onFalse(
            Commands.parallel(
                m_intake.setRollerRPM(() -> 0),
                m_feeder.setRPM(() -> 0)));

    // Right bumper for run intake backward
    m_operator.rightBumper().onTrue(
        Commands.parallel(
            m_intake.setRollerRPM(() -> -3000),
            m_feeder.setRPM(() -> -3000)))
        .onFalse(
            Commands.parallel(
                m_intake.setRollerRPM(() -> 0),
                m_feeder.setRPM(() -> 0)));

    // Y for shooter at subwoofer
    m_operator.y().onTrue(
        Commands.parallel(
            m_pivot.setPivotTarget(() -> Units.radiansToDegrees(56)),
            m_shooter.setRPM(() -> 5800, 0.3))
            .andThen(m_feeder.setRPM(() -> 2000)
                .until(() -> (!m_feeder.feederBeambreakObstructed() && !m_feeder.shooterBeambreakObstructed()))));

    // X for shooter at amp

    // B for shooter at podium or feeding

    // A for shooter at source
    // m_operator.a().onTrue(
    //     Commands.parallel(
    //         m_shooter.setRPM(() -> -3000, 1.0),
    //         m_feeder.setRPM(() -> -3000))
    //         .andThen(m_feeder.setRPM(() -> 3000)
    //             .until(() -> (m_feeder.feederBeambreakObstructed() && !m_feeder.shooterBeambreakObstructed()))));


    m_operator.leftTrigger(0.1).onTrue(
        m_shooter.setRPM(() -> 5800, 0.3)
    ).onFalse(m_shooter.setRPM(() -> 0, 1));

  }

  public void configurePathplanner() {
    NamedCommands.registerCommand(
        "ShootNote",
        Commands.parallel(
            m_shooter.setRPM(() -> 5800, 0.3),
            Commands.sequence(
                new WaitCommand(1),
                m_feeder.setRPM(() -> 2000)
            )
        ).until(
            () -> (!m_feeder.feederBeambreakObstructed() && !m_feeder.shooterBeambreakObstructed())
        )
    );

    m_operator.leftBumper().onTrue(
        m_shooter.stopShooter());

  }

  public void robotPeriodic() {
    m_visualizer.periodic();
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    return null;
  }

  private static double teleopAxisAdjustment(double x) {
    return MathUtil.applyDeadband(Math.abs(Math.pow(x, 2)) * Math.signum(x), 0.02);
  }
}